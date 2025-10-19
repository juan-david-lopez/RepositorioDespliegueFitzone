package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.CreateMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.ProcessPaymentRequest;
import co.edu.uniquindio.FitZone.dto.request.SuspendMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.RenewMembershipRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipStatusResponse;
import co.edu.uniquindio.FitZone.dto.response.ProcessPaymentResponse;
import co.edu.uniquindio.FitZone.exception.LocationNotFoundException;
import co.edu.uniquindio.FitZone.exception.MembershipTypeNotFoundException;
import co.edu.uniquindio.FitZone.exception.ResourceAlreadyExistsException;
import co.edu.uniquindio.FitZone.exception.UserNotFoundException;
import co.edu.uniquindio.FitZone.integration.payment.StripeService;
import co.edu.uniquindio.FitZone.model.entity.Location;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.model.enums.ActivityType;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.repository.LocationRepository;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.service.interfaces.ILoyaltyService;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementación del servicio de membresías.
 * Maneja la lógica de negocio relacionada con la creación, suspensión,
 * reactivación y cancelación de membresías.
 */
@Service
public class MembershipServiceImpl implements IMembershipService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipServiceImpl.class);

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final LocationRepository locationRepository;
    private final StripeService stripeService;
    private final ILoyaltyService loyaltyService;

    public MembershipServiceImpl(MembershipRepository membershipRepository, UserRepository userRepository, MembershipTypeRepository membershipTypeRepository, LocationRepository locationRepository, StripeService stripeService, ILoyaltyService loyaltyService) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.membershipTypeRepository = membershipTypeRepository;
        this.locationRepository = locationRepository;
        this.stripeService = stripeService;
        this.loyaltyService = loyaltyService;
    }


    @Override
    public MembershipResponse createMembership(CreateMembershipRequest request) {
        logger.info("📝 Iniciando creación de membresía para userId: {}", request.userId());
        logger.debug("Datos de la membresía - Tipo: {}, Sede: {}, PaymentIntent: {}",
            request.MembershipTypeId(), request.mainLocationId(), request.paymentIntentId());

        // ============================================
        // PASO 1: Validar formato del PaymentIntent
        // ============================================
        if (!request.paymentIntentId().startsWith("pi_")) {
            logger.error("❌ Formato de paymentIntentId inválido. Debe ser pi_xxx, recibido: {}", request.paymentIntentId());
            throw new IllegalArgumentException(
                "Formato de paymentIntentId inválido. Debe ser pi_xxx, recibido: " + request.paymentIntentId()
            );
        }

        // ============================================
        // PASO 2: Validar que los recursos existan
        // ============================================
        User user = userRepository.findById(request.userId())
                .orElseThrow( () -> {
                    logger.error("❌ Usuario no encontrado para crear membresía con ID: {}", request.userId());
                    return new UserNotFoundException("El usuario no existe");
                });

        logger.debug("✓ Usuario encontrado: {} (ID: {})", user.getPersonalInformation().getFirstName(), request.userId());

        if(user.getMembership() != null){
            logger.debug("⚠️ Usuario ya tiene membresía con estado: {}", user.getMembership().getStatus());

            if(user.getMembership().getStatus() == MembershipStatus.ACTIVE){
                logger.error("❌ Intento de crear membresía para usuario con membresía activa - ID: {}", request.userId());
                throw new ResourceAlreadyExistsException("El usuario ya tiene una membresía activa");
            }

            if(user.getMembership().getStatus() == MembershipStatus.SUSPENDED ){
                logger.error("❌ Intento de crear membresía para usuario con membresía suspendida - ID: {}, Razón: {}",
                    request.userId(), user.getMembership().getSuspensionReason());
                throw new ResourceAlreadyExistsException("El usuario ya tiene una membresía registrada, " +
                        "pero esta se encuentra suspendida por la siguiente razón: " + user.getMembership().getSuspensionReason());
            }
        }

        logger.debug("🔍 Validando tipo de membresía con ID: {}", request.MembershipTypeId());
        MembershipType type = membershipTypeRepository.findById(request.MembershipTypeId())
                .orElseThrow(()-> {
                    logger.error("❌ Tipo de membresía no encontrado con ID: {}", request.MembershipTypeId());
                    return new MembershipTypeNotFoundException("Tipo de membresía no encontrada en el sistema");
                });

        logger.debug("✓ Tipo de membresía encontrado: {}", type.getName());

        logger.debug("🔍 Validando sede principal con ID: {}", request.mainLocationId());
        Location location = locationRepository.findById(request.mainLocationId())
                .orElseThrow(()-> {
                    logger.error("❌ Sede principal no encontrada con ID: {}", request.mainLocationId());
                    return new LocationNotFoundException("Sede principal no encontrada");
                });

        logger.debug("✓ Sede encontrada: {}", location.getName());

        try{
            // ============================================
            // PASO 3: Recuperar PaymentIntent de Stripe
            // ============================================
            logger.info("🔍 Validando PaymentIntent en Stripe: {}", request.paymentIntentId());

            PaymentIntent paymentIntent = stripeService.getPaymentIntent(request.paymentIntentId());

            if (paymentIntent == null) {
                logger.error("❌ PaymentIntent no encontrado en Stripe: {}", request.paymentIntentId());
                throw new IllegalArgumentException(
                    "PaymentIntent no encontrado en Stripe: " + request.paymentIntentId()
                );
            }

            // ============================================
            // PASO 4: Verificar que el pago fue exitoso
            // ============================================
            logger.info("✓ PaymentIntent encontrado. Status: {}", paymentIntent.getStatus());

            if(!"succeeded".equals(paymentIntent.getStatus())){
                logger.error("❌ PaymentIntent no fue completado exitosamente. Status: {}", paymentIntent.getStatus());
                throw new IllegalStateException(
                    "PaymentIntent no fue completado exitosamente. Status: " + paymentIntent.getStatus()
                );
            }

            // ============================================
            // PASO 5: Validar monto y moneda (opcional pero recomendado)
            // ============================================
           BigDecimal valor = type.getMonthlyPrice();
            long amountInCents = valor.multiply(BigDecimal.valueOf(100)).longValue();

            logger.info("💰 Validando monto - Esperado: {} centavos, Recibido: {}",
                       amountInCents, paymentIntent.getAmount());

            if (paymentIntent.getAmount() != amountInCents) {
                logger.warn("⚠️ Monto no coincide exactamente - Esperado: {}, Recibido: {} (puede ser descuento o ajuste)",
                    amountInCents, paymentIntent.getAmount());
                // Permitir una diferencia razonable (ej: 10%) por descuentos o ajustes
                long difference = Math.abs(paymentIntent.getAmount() - amountInCents);
                double percentageDiff = (difference * 100.0) / amountInCents;
                if (percentageDiff > 50) { // Si la diferencia es mayor al 50%, rechazar
                    logger.error("❌ Diferencia de monto muy grande: {}%", percentageDiff);
                    throw new IllegalStateException("El monto pagado no coincide con el precio de la membresía");
                }
            }

            // ============================================
            // PASO 6: Crear registro de membresía
            // ============================================
            logger.info("💾 Creando registro de membresía en BD");

            Membership newMembership = new Membership();
            newMembership.setUser(user);
            newMembership.setType(type);
            newMembership.setLocation(location);
            newMembership.setPrice(type.getMonthlyPrice());
            newMembership.setStartDate(LocalDate.now());
            newMembership.setEndDate(LocalDate.now().plusMonths(1));
            newMembership.setStatus(MembershipStatus.ACTIVE);

            Membership savedMembership = membershipRepository.save(newMembership);
            logger.info("✓ Membresía guardada con ID: {}", savedMembership.getIdMembership());

            //Actualizamos la referencia de la membresía en el usuario
            logger.debug("🔄 Actualizando referencia de membresía en el usuario");

            // Registrar actividad de fidelización por compra de membresía
            try {
                loyaltyService.logActivityAutomatic(
                    user.getIdUser(),
                    ActivityType.MEMBERSHIP_PURCHASE,
                    "Compra de membresía " + type.getName(),
                    savedMembership.getIdMembership()
                );
                logger.debug("✓ Actividad de fidelización registrada para compra de membresía");
            } catch (Exception e) {
                logger.warn("⚠️ No se pudo registrar actividad de fidelización: {}", e.getMessage());
            }

            user.setMembership(savedMembership);
            user.setMembershipType(type.getName());  // type.getName() ya retorna MembershipTypeName
            user.setMainLocation(location);
            userRepository.save(user);

            logger.info("✅ Membresía creada exitosamente - ID: {}, Usuario: {}, Tipo: {}",
                savedMembership.getIdMembership(), user.getPersonalInformation().getFirstName(), type.getName());

            //Retornamos la respuesta al cliente
            return new MembershipResponse(
                    savedMembership.getIdMembership(),
                    user.getIdUser(),
                    type.getName(),
                    location.getIdLocation(),
                    savedMembership.getStartDate(),
                    savedMembership.getEndDate(),
                    savedMembership.getStatus()
            );

        } catch (IllegalArgumentException e) {
            logger.error("❌ Identificador de pago inválido recibido: {}. Detalle: {}", request.paymentIntentId(), e.getMessage());
            throw new co.edu.uniquindio.FitZone.exception.InvalidPaymentIdentifierException("Identificador de pago inválido: " + e.getMessage());
        } catch (StripeException e) {
            logger.error("❌ Error al verificar el pago con Stripe - PaymentIntent: {}, Error: {}",
                request.paymentIntentId(), e.getMessage(), e);
            throw new RuntimeException("Error al verificar el pago con Stripe: " + e.getMessage());
        }
    }

    @Override
    public MembershipResponse getMembershipByUserId(Long userId) {
        logger.debug("Consultando membresía por ID de usuario: {}", userId);

        // ✅ CORREGIDO: Usar el repositorio de membresías directamente para evitar problemas de LAZY loading
        List<Membership> memberships = membershipRepository.findByUserIdOrderByStartDateDesc(userId);

        if (memberships == null || memberships.isEmpty()) {
            logger.warn("Usuario sin membresías registradas - ID: {}", userId);
            throw new MembershipTypeNotFoundException("El usuario no tiene una membresía activa");
        }

        // Obtener la membresía más reciente (primera en la lista ordenada por fecha descendente)
        Membership membership = memberships.getFirst();

        // Verificar que la membresía esté activa
        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            logger.warn("Membresía encontrada pero no está activa - ID: {}, Estado: {}",
                membership.getIdMembership(), membership.getStatus());
            throw new MembershipTypeNotFoundException("El usuario no tiene una membresía activa");
        }

        logger.info("✅ [getMembershipByUserId] Membresía activa encontrada - Usuario: {}, Membership ID: {}, Tipo: {}",
            userId, membership.getIdMembership(), membership.getType().getName());

        return new MembershipResponse(
                membership.getIdMembership(),
                membership.getUser().getIdUser(),
                membership.getType().getName(),
                membership.getLocation().getIdLocation(),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getStatus()
        );
    }

    @Override
    public MembershipResponse getMembershipByDocumentNumber(String documentNumber) {
        logger.debug("Consultando membresía por número de documento: {}", documentNumber);

        User user = userRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado con número de documento: {}", documentNumber);
                    return new UserNotFoundException("El usuario no existe");
                });

        if (user.getMembership() == null) {
            logger.warn("Usuario sin membresía activa - Documento: {}", documentNumber);
            throw new MembershipTypeNotFoundException("El usuario no tiene una membresía activa");
        }

        logger.debug("Membresía encontrada para usuario: {} (Documento: {})", 
            user.getPersonalInformation().getFirstName(), documentNumber);
        
        Membership membership = user.getMembership();
        return new MembershipResponse(
                membership.getIdMembership(),
                membership.getUser().getIdUser(),
                membership.getType().getName(),
                membership.getLocation().getIdLocation(),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getStatus()
        );
    }

    @Override
    public MembershipResponse suspendMembership(SuspendMembershipRequest request) {
        logger.info("Iniciando suspensión de membresía para usuario ID: {}", request.userId());
        logger.debug("Razón de suspensión: {}, Fecha fin suspensión: {}", 
            request.suspensionReason(), request.suspensionEnd());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para suspender membresía con ID: {}", request.userId());
                    return new UserNotFoundException("El usuario no existe");
                });
        
        Membership membership = user.getMembership();

        if (membership == null) {
            logger.error("Usuario sin membresía para suspender - ID: {}", request.userId());
            throw new MembershipTypeNotFoundException("El usuario no tiene una membresía");
        }

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            logger.warn("Intento de suspender membresía no activa - ID: {}, Estado: {}", 
                membership.getIdMembership(), membership.getStatus());
            throw new ResourceAlreadyExistsException("La membresía ya está suspendida o cancelada");
        }

        logger.debug("Suspendiendo membresía activa - ID: {}", membership.getIdMembership());
        membership.setStatus(MembershipStatus.SUSPENDED);
        membership.setSuspensionReason(request.suspensionReason());
        membership.setSuspensionStart(LocalDate.now());
        membership.setSuspensionEnd(request.suspensionEnd());

        Membership updatedMembership = membershipRepository.save(membership);
        logger.info("Membresía suspendida exitosamente - ID: {}, Usuario: {}, Razón: {}", 
            updatedMembership.getIdMembership(), user.getPersonalInformation().getFirstName(), request.suspensionReason());

        return new MembershipResponse(
                updatedMembership.getIdMembership(),
                updatedMembership.getUser().getIdUser(),
                updatedMembership.getType().getName(),
                updatedMembership.getLocation().getIdLocation(),
                updatedMembership.getStartDate(),
                updatedMembership.getEndDate(),
                updatedMembership.getStatus()
        );
    }

    @Override
    public MembershipResponse reactivateMembership(Long userId) {
        logger.info("Iniciando reactivación de membresía para usuario ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para reactivar membresía con ID: {}", userId);
                    return new UserNotFoundException("El usuario no existe");
                });
        
        Membership membership = user.getMembership();

        if (membership == null || membership.getStatus() != MembershipStatus.SUSPENDED) {
            logger.warn("Intento de reactivar membresía no suspendida - ID: {}, Estado: {}", 
                membership != null ? membership.getIdMembership() : "null", 
                membership != null ? membership.getStatus() : "null");
            throw new RuntimeException("La membresía no puede ser reactivada ya que esta no se encuentra suspendida");
        }

        logger.debug("Reactivando membresía suspendida - ID: {}", membership.getIdMembership());
        
        // Calcula la duración real de la suspensión y extiende la fecha de finalización.
        long suspensionDays = ChronoUnit.DAYS.between(membership.getSuspensionStart(), LocalDate.now());
        logger.debug("Días de suspensión calculados: {}, extendiendo fecha de finalización", suspensionDays);
        
        membership.setEndDate(membership.getEndDate().plusDays(suspensionDays));

        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setSuspensionReason(null);
        membership.setSuspensionStart(null);
        membership.setSuspensionEnd(null);

        Membership updatedMembership = membershipRepository.save(membership);
        logger.info("Membresía reactivada exitosamente - ID: {}, Usuario: {}, Nueva fecha fin: {}", 
            updatedMembership.getIdMembership(), user.getPersonalInformation().getFirstName(), updatedMembership.getEndDate());

        return new MembershipResponse(
                updatedMembership.getIdMembership(),
                updatedMembership.getUser().getIdUser(),
                updatedMembership.getType().getName(),
                updatedMembership.getLocation().getIdLocation(),
                updatedMembership.getStartDate(),
                updatedMembership.getEndDate(),
                updatedMembership.getStatus()
        );
    }
    @Override
    public void cancelMembership(Long userId) {
        logger.info("Iniciando cancelación de membresía para usuario ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para cancelar membresía con ID: {}", userId);
                    return new UserNotFoundException("El usuario no existe");
                });
        
        Membership membership = user.getMembership();

        if (membership == null) {
            logger.error("Usuario sin membresía para cancelar - ID: {}", userId);
            throw new MembershipTypeNotFoundException("El usuario no tiene una membresía");
        }

        logger.debug("Cancelando membresía - ID: {}, Estado actual: {}", 
            membership.getIdMembership(), membership.getStatus());
        
        membership.setStatus(MembershipStatus.CANCELLED);
        membershipRepository.save(membership);
        
        logger.info("Membresía cancelada exitosamente - ID: {}, Usuario: {}", 
            membership.getIdMembership(), user.getPersonalInformation().getFirstName());
    }

    @Override
    public MembershipStatusResponse checkMembershipStatus(Long userId) {
        logger.info("Verificando estado de membresía para usuario ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado al verificar membresía - ID: {}", userId);
                    return new UserNotFoundException("El usuario no existe");
                });

        Membership membership = user.getMembership();
        
        if (membership == null) {
            logger.warn("El usuario no tiene una membresía activa - ID: {}", userId);
            return MembershipStatusResponse.createInactiveResponse("El usuario no tiene una membresía activa");
        }

        LocalDate today = LocalDate.now();
        
        if (membership.getStatus() == MembershipStatus.ACTIVE) {
            if (today.isAfter(membership.getEndDate())) {
                // La membresía ha expirado
                membership.setStatus(MembershipStatus.EXPIRED);
                membershipRepository.save(membership);
                logger.info("Membresía marcada como expirada - ID: {}, Fecha de vencimiento: {}", 
                    membership.getIdMembership(), membership.getEndDate());
                return MembershipStatusResponse.createInactiveResponse(
                        "Su membresía ha expirado el " + membership.getEndDate(),
                        MembershipStatus.EXPIRED
                );
            }
            
            // La membresía está activa y no ha expirado
            logger.info("Membresía activa encontrada - ID: {}, Válida hasta: {}", 
                membership.getIdMembership(), membership.getEndDate());
            return MembershipStatusResponse.createActiveResponse(membership.getEndDate());
            
        } else if (membership.getStatus() == MembershipStatus.SUSPENDED) {
            logger.info("Membresía suspendida - ID: {}, Razón: {}", 
                membership.getIdMembership(), membership.getSuspensionReason());
            return MembershipStatusResponse.createInactiveResponse(
                    "Membresía suspendida: " + membership.getSuspensionReason(),
                    MembershipStatus.SUSPENDED
            );
            
        } else if (membership.getStatus() == MembershipStatus.EXPIRED) {
            logger.info("Membresía expirada - ID: {}, Fecha de vencimiento: {}", 
                membership.getIdMembership(), membership.getEndDate());
            return MembershipStatusResponse.createInactiveResponse(
                    "Su membresía expiró el " + membership.getEndDate(),
                    MembershipStatus.EXPIRED
            );
            
        } else if (membership.getStatus() == MembershipStatus.CANCELLED) {
            logger.info("Membresía cancelada - ID: {}", membership.getIdMembership());
            return MembershipStatusResponse.createInactiveResponse(
                    "Su membresía ha sido cancelada",
                    MembershipStatus.CANCELLED
            );
        }

        // Estado no reconocido
        logger.warn("Estado de membresía no reconocido - ID: {}, Estado: {}", 
            membership.getIdMembership(), membership.getStatus());
        return MembershipStatusResponse.createInactiveResponse(
                "Estado de membresía no reconocido",
                membership.getStatus()
        );
    }

    @Override
    public MembershipResponse renewMembership(RenewMembershipRequest request) {
        logger.info("Iniciando renovación de membresía para usuario ID: {}", request.userId());
        logger.debug("Datos de renovación - Nuevo tipo: {}, Duración: {} meses, Descuento estudiantil: {}",
            request.newMembershipType(), request.durationMonths(), request.hasStudentDiscount());

        // Validar que el usuario exista
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para renovar membresía con ID: {}", request.userId());
                    return new UserNotFoundException("El usuario no existe");
                });

        // Validar que el usuario tenga una membresía
        Membership currentMembership = user.getMembership();
        if (currentMembership == null) {
            logger.error("Usuario sin membresía para renovar - ID: {}", request.userId());
            throw new MembershipTypeNotFoundException("El usuario no tiene una membresía para renovar");
        }

        // Solo se pueden renovar membresías activas o expiradas
        if (currentMembership.getStatus() == MembershipStatus.CANCELLED) {
            logger.warn("Intento de renovar membresía cancelada - ID: {}", currentMembership.getIdMembership());
            throw new RuntimeException("No se puede renovar una membresía cancelada");
        }

        // Obtener el nuevo tipo de membresía
        MembershipType newType = membershipTypeRepository.findByName(request.newMembershipType())
                .orElseThrow(() -> {
                    logger.error("Tipo de membresía no encontrado: {}", request.newMembershipType());
                    return new MembershipTypeNotFoundException("Tipo de membresía no encontrado");
                });

        try {
            // Verificar el pago con Stripe
            PaymentIntent paymentIntent = stripeService.getPaymentIntent(request.paymentIntentId());

            if (!"succeeded".equals(paymentIntent.getStatus())) {
                logger.error("Pago no completado correctamente para renovación - PaymentIntent: {}, Estado: {}",
                    request.paymentIntentId(), paymentIntent.getStatus());
                throw new RuntimeException("El pago no se ha completado correctamente");
            }

            logger.debug("Pago verificado exitosamente, renovando membresía");

            // Actualizar la membresía existente
            currentMembership.setType(newType);
            currentMembership.setPrice(newType.getMonthlyPrice());
            currentMembership.setStatus(MembershipStatus.ACTIVE);

            // Calcular nueva fecha de finalización
            LocalDate newStartDate = LocalDate.now();
            if (currentMembership.getStatus() == MembershipStatus.ACTIVE &&
                currentMembership.getEndDate().isAfter(LocalDate.now())) {
                // Si la membresía aún está activa, extender desde la fecha actual de finalización
                newStartDate = currentMembership.getEndDate();
            }

            currentMembership.setStartDate(newStartDate);
            currentMembership.setEndDate(newStartDate.plusMonths(request.durationMonths()));

            // Limpiar datos de suspensión si los hay
            currentMembership.setSuspensionReason(null);
            currentMembership.setSuspensionStart(null);
            currentMembership.setSuspensionEnd(null);

            Membership renewedMembership = membershipRepository.save(currentMembership);

            // Registrar actividad de fidelización por renovación
            try {
                boolean isEarlyRenewal = currentMembership.getEndDate().isAfter(LocalDate.now().plusDays(7));
                ActivityType activityType = isEarlyRenewal ? ActivityType.EARLY_RENEWAL : ActivityType.MEMBERSHIP_RENEWAL;

                loyaltyService.logActivityAutomatic(
                    request.userId(),
                    activityType,
                    "Renovación de membresía a " + newType.getName() + (isEarlyRenewal ? " (anticipada)" : ""),
                    renewedMembership.getIdMembership()
                );
                logger.debug("Actividad de fidelización registrada para renovación de membresía");
            } catch (Exception e) {
                logger.warn("No se pudo registrar actividad de fidelización: {}", e.getMessage());
            }

            logger.info("Membresía renovada exitosamente - ID: {}, Nuevo tipo: {}, Nueva fecha fin: {}",
                renewedMembership.getIdMembership(), newType.getName(), renewedMembership.getEndDate());

            return new MembershipResponse(
                    renewedMembership.getIdMembership(),
                    renewedMembership.getUser().getIdUser(),
                    renewedMembership.getType().getName(),
                    renewedMembership.getLocation().getIdLocation(),
                    renewedMembership.getStartDate(),
                    renewedMembership.getEndDate(),
                    renewedMembership.getStatus()
            );

        } catch (StripeException e) {
            logger.error("Error al verificar el pago con Stripe para renovación - PaymentIntent: {}, Error: {}",
                request.paymentIntentId(), e.getMessage(), e);
            throw new RuntimeException("Error al verificar el pago con Stripe: " + e.getMessage());
        }
    }

    @Override
    public ProcessPaymentResponse processPaymentAndCreateMembership(ProcessPaymentRequest request) throws StripeException {
        logger.info("🔄 Iniciando procesamiento de pago para userId: {}", request.userId());
        logger.debug("Datos - MembershipType: {}, Location: {}, PaymentMethod: {}",
                    request.membershipTypeId(), request.mainLocationId(), request.paymentMethodId());

        // ================================================
        // PASO 1: Validar formato del PaymentMethod
        // ================================================
        if (!request.paymentMethodId().startsWith("pm_")) {
            logger.error("❌ Formato de paymentMethodId inválido. Debe ser pm_xxx, recibido: {}", request.paymentMethodId());
            throw new IllegalArgumentException(
                "Formato de paymentMethodId inválido. Debe ser pm_xxx, recibido: " + request.paymentMethodId()
            );
        }

        // ================================================
        // PASO 2: Obtener datos del usuario y membresía
        // ================================================
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> {
                logger.error("❌ Usuario no encontrado con ID: {}", request.userId());
                return new UserNotFoundException("El usuario no existe");
            });

        logger.debug("✓ Usuario encontrado: {} (ID: {})", user.getPersonalInformation().getFirstName(), request.userId());

        // Validar que no tenga membresía activa
        if (user.getMembership() != null && user.getMembership().getStatus() == MembershipStatus.ACTIVE) {
            logger.error("❌ Usuario ya tiene membresía activa - ID: {}", request.userId());
            throw new ResourceAlreadyExistsException("El usuario ya tiene una membresía activa");
        }

        MembershipType type = membershipTypeRepository.findById(request.membershipTypeId())
            .orElseThrow(() -> {
                logger.error("❌ Tipo de membresía no encontrado con ID: {}", request.membershipTypeId());
                return new MembershipTypeNotFoundException("Tipo de membresía no encontrada en el sistema");
            });

        logger.debug("✓ Tipo de membresía encontrado: {}", type.getName());

        Location location = locationRepository.findById(request.mainLocationId())
            .orElseThrow(() -> {
                logger.error("❌ Sede principal no encontrada con ID: {}", request.mainLocationId());
                return new LocationNotFoundException("Sede principal no encontrada");
            });

        logger.debug("✓ Sede encontrada: {}", location.getName());

        // ================================================
        // PASO 3: Crear PaymentIntent usando Secret Key
        // ================================================
        logger.info("💳 Creando PaymentIntent con Stripe (usando Secret Key)...");

        BigDecimal valor = type.getMonthlyPrice();
        long amountInCents = valor.multiply(BigDecimal.valueOf(100)).longValue();

        String description = String.format("Membresía %s - Usuario: %s %s",
                                          type.getName(),
                                          user.getPersonalInformation().getFirstName(),
                                          user.getPersonalInformation().getLastName());

        PaymentIntent paymentIntent = stripeService.createAndConfirmPaymentIntent(
            amountInCents,
            "cop",  // Moneda colombiana
            request.paymentMethodId(),
            description
        );

        logger.info("✓ PaymentIntent creado: {}", paymentIntent.getId());

        // ================================================
        // PASO 4: Verificar que el pago fue exitoso
        // ================================================
        logger.info("🔍 Verificando status del PaymentIntent: {}", paymentIntent.getStatus());

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            logger.error("❌ Pago no fue exitoso. Status: {}", paymentIntent.getStatus());

            // Si requiere autenticación adicional (3D Secure)
            if ("requires_action".equals(paymentIntent.getStatus())) {
                throw new IllegalStateException(
                    "El pago requiere autenticación adicional. Client Secret: " + paymentIntent.getClientSecret()
                );
            }

            throw new IllegalStateException("Pago fallido. Status: " + paymentIntent.getStatus());
        }

        logger.info("✅ Pago confirmado exitosamente");

        // ================================================
        // PASO 5: Crear registro de membresía
        // ================================================
        logger.info("💾 Creando registro de membresía en BD...");

        Membership newMembership = new Membership();
        newMembership.setUser(user);
        newMembership.setType(type);
        newMembership.setLocation(location);
        newMembership.setPrice(type.getMonthlyPrice());
        newMembership.setStartDate(LocalDate.now());
        newMembership.setEndDate(LocalDate.now().plusMonths(1));
        newMembership.setStatus(MembershipStatus.ACTIVE);

        Membership savedMembership = membershipRepository.save(newMembership);
        logger.info("✓ Membresía guardada con ID: {}", savedMembership.getIdMembership());

        // ================================================
        // PASO 6: Actualizar usuario
        // ================================================
        logger.debug("🔄 Actualizando referencia de membresía en el usuario");

        // Registrar actividad de fidelización por compra de membresía
        try {
            loyaltyService.logActivityAutomatic(
                user.getIdUser(),
                ActivityType.MEMBERSHIP_PURCHASE,
                "Compra de membresía " + type.getName(),
                savedMembership.getIdMembership()
            );
            logger.debug("✓ Actividad de fidelización registrada para compra de membresía");
        } catch (Exception e) {
            logger.warn("⚠️ No se pudo registrar actividad de fidelización: {}", e.getMessage());
        }

        user.setMembership(savedMembership);
        user.setMembershipType(type.getName());  // type.getName() ya retorna MembershipTypeName
        user.setMainLocation(location);
        userRepository.save(user);

        logger.info("✅ Membresía creada exitosamente - ID: {}, Usuario: {}, Tipo: {}, PaymentIntent: {}",
            savedMembership.getIdMembership(), user.getPersonalInformation().getFirstName(),
            type.getName(), paymentIntent.getId());

        // ================================================
        // PASO 7: Retornar respuesta
        // ================================================
        return ProcessPaymentResponse.builder()
            .success(true)
            .membershipId(savedMembership.getIdMembership())
            .userId(user.getIdUser())
            .membershipTypeName(type.getName().toString())  // Convertir enum a String
            .paymentIntentId(paymentIntent.getId())
            .status(savedMembership.getStatus())
            .startDate(savedMembership.getStartDate())
            .endDate(savedMembership.getEndDate())
            .message("Membresía activada exitosamente")
            .build();
    }
}
