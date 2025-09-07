package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.CreateMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.SuspendMembershipRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;
import co.edu.uniquindio.FitZone.exception.LocationNotFoundException;
import co.edu.uniquindio.FitZone.exception.MembershipTypeNotFoundException;
import co.edu.uniquindio.FitZone.exception.ResourceAlreadyExistsException;
import co.edu.uniquindio.FitZone.exception.UserNotFoundException;
import co.edu.uniquindio.FitZone.integration.payment.StripeService;
import co.edu.uniquindio.FitZone.model.entity.Location;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.repository.LocationRepository;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    public MembershipServiceImpl(MembershipRepository membershipRepository, UserRepository userRepository, MembershipTypeRepository membershipTypeRepository, LocationRepository locationRepository, StripeService stripeService) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.membershipTypeRepository = membershipTypeRepository;
        this.locationRepository = locationRepository;
        this.stripeService = stripeService;
    }


    @Override
    public MembershipResponse createMembership(CreateMembershipRequest request) {
        logger.info("Iniciando creación de membresía para usuario ID: {}", request.userId());
        logger.debug("Datos de la membresía - Tipo: {}, Sede: {}, PaymentIntent: {}", 
            request.MembershipTypeId(), request.mainLocationId(), request.paymentIntentId());

        //Validar que los recursos existan
        User user = userRepository.findById(request.userId())
                .orElseThrow( () -> {
                    logger.error("Usuario no encontrado para crear membresía con ID: {}", request.userId());
                    return new UserNotFoundException("El usuario no existe");
                });

        logger.debug("Usuario encontrado: {} (ID: {})", user.getPersonalInformation().getFirstName(), request.userId());

        if(user.getMembership() != null){
            logger.debug("Usuario ya tiene membresía con estado: {}", user.getMembership().getStatus());

            if(user.getMembership().getStatus() == MembershipStatus.ACTIVE){
                logger.warn("Intento de crear membresía para usuario con membresía activa - ID: {}", request.userId());
                throw new ResourceAlreadyExistsException("El usuario ya tiene una membresía activa");
            }

            if(user.getMembership().getStatus() == MembershipStatus.SUSPENDED ){
                logger.warn("Intento de crear membresía para usuario con membresía suspendida - ID: {}, Razón: {}", 
                    request.userId(), user.getMembership().getSuspensionReason());
                throw new ResourceAlreadyExistsException("El usuario ya tiene una membresía registrada, " +
                        "pero esta se encuentra suspendida por la siguiente razón: " + user.getMembership().getSuspensionReason());
            }
        }

        logger.debug("Validando tipo de membresía con ID: {}", request.MembershipTypeId());
        MembershipType type = membershipTypeRepository.findById(request.MembershipTypeId())
                .orElseThrow(()-> {
                    logger.error("Tipo de membresía no encontrado con ID: {}", request.MembershipTypeId());
                    return new MembershipTypeNotFoundException("Tipo de membresía no encontrada en el sistema");
                });

        logger.debug("Validando sede principal con ID: {}", request.mainLocationId());
        Location location = locationRepository.findById(request.mainLocationId())
                .orElseThrow(()-> {
                    logger.error("Sede principal no encontrada con ID: {}", request.mainLocationId());
                    return new LocationNotFoundException("Sede principal no encontrada");
                });

        try{
            logger.debug("Verificando pago con Stripe - PaymentIntent: {}", request.paymentIntentId());
            //Verificar el pago con Stripe usando el ID de la intención de pago
            PaymentIntent paymentIntent = stripeService.getPaymentIntent(request.paymentIntentId());

            if(!"succeeded".equals(paymentIntent.getStatus())){
                logger.error("Pago no completado correctamente - PaymentIntent: {}, Estado: {}", 
                    request.paymentIntentId(), paymentIntent.getStatus());
                throw new RuntimeException("El pago no se ha completado correctamente");
            }

            logger.debug("Pago verificado exitosamente, creando membresía en la base de datos");
            // Creamos y guardamos la membresía en la base de datos
            Membership newMembership = new Membership();
            newMembership.setUser(user);
            newMembership.setType(type);
            newMembership.setLocation(location);
            newMembership.setPrice(type.getMonthlyPrice());
            newMembership.setStartDate(LocalDate.now());
            newMembership.setEndDate(LocalDate.now().plusMonths(1));
            newMembership.setStatus(MembershipStatus.ACTIVE);

            Membership savedMembership = membershipRepository.save(newMembership);
            logger.debug("Membresía guardada con ID: {}", savedMembership.getIdMembership());

            //Actualizamos la referencia de la membresía en el usuario
            logger.debug("Actualizando referencia de membresía en el usuario");
            user.setMembership(savedMembership);
            user.setMainLocation(location);
            userRepository.save(user);

            logger.info("Membresía creada exitosamente - ID: {}, Usuario: {}, Tipo: {}", 
                savedMembership.getIdMembership(), user.getPersonalInformation().getFirstName(), type.getName());

            //Retornamos la respuesta al cliente
            return new MembershipResponse(
                    savedMembership.getIdMembership(),
                    savedMembership.getUser().getIdUser(),
                    savedMembership.getType().getName(),
                    savedMembership.getLocation().getIdLocation(),
                    savedMembership.getStartDate(),
                    savedMembership.getEndDate(),
                    savedMembership.getStatus()
            );

        } catch (StripeException e) {
            logger.error("Error al verificar el pago con Stripe - PaymentIntent: {}, Error: {}", 
                request.paymentIntentId(), e.getMessage(), e);
            throw new RuntimeException("Error al verificar el pago con Stripe: " + e.getMessage());
        }
    }

    @Override
    public MembershipResponse getMembershipByUserId(Long userId) {
        logger.debug("Consultando membresía por ID de usuario: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para consultar membresía con ID: {}", userId);
                    return new UserNotFoundException("El usuario no existe");
                });

        if (user.getMembership() == null) {
            logger.warn("Usuario sin membresía activa - ID: {}", userId);
            throw new MembershipTypeNotFoundException("El usuario no tiene una membresía activa");
        }

        logger.debug("Membresía encontrada para usuario: {} (ID: {})", 
            user.getPersonalInformation().getFirstName(), userId);
        
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
    public MembershipResponse getMembershipByDocumentNumber(String documentNumber) {
        logger.debug("Consultando membresía por número de documento: {}", documentNumber);

        User user = userRepository.findByPersonalInformation_DocumentNumber(documentNumber)
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
}
