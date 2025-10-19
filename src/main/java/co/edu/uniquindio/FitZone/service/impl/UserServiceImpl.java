package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.LoginRequest;
import co.edu.uniquindio.FitZone.dto.request.UserRequest;
import co.edu.uniquindio.FitZone.dto.request.UserUpdateRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;
import co.edu.uniquindio.FitZone.dto.response.UserResponse;
import co.edu.uniquindio.FitZone.exception.LocationNotFoundException;
import co.edu.uniquindio.FitZone.exception.ResourceAlreadyExistsException;
import co.edu.uniquindio.FitZone.exception.UnauthorizedRegistrationException;
import co.edu.uniquindio.FitZone.exception.UserNotFoundException;
import co.edu.uniquindio.FitZone.model.entity.Location;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.PersonalInformation;
import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import co.edu.uniquindio.FitZone.repository.LocationRepository;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import co.edu.uniquindio.FitZone.service.interfaces.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Clase SERVICE - Que implementa los métodos declarados en IUserService
 */
@Service
public class UserServiceImpl implements IUserService{

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final IMembershipService membershipService; // ✅ AGREGADO

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          LocationRepository locationRepository, IMembershipService membershipService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationRepository = locationRepository;
        this.membershipService = membershipService; // ✅ AGREGADO
    }


    /**
     * -----------FALTA VINCULAR LA SEDE --------------
     * Método para registrar un usuario
     * @param request objeto que contiene los datos del usuario a registrar
     * @return UserResponse objeto de respuesta, contiene alguna información del usuario registrado
     */
    @Override
    public UserResponse registerUser(UserRequest request) {
        logger.info("Iniciando registro de usuario por administrador - Email: {}, Rol: {}",
                request.email(), request.role());

        //Obtener el rol del usuario autenticado que está realizando la solicitud
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserRole registeringUserRole = UserRole.valueOf(authentication.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Rol de usuario autenticado no encontrado para el registro");
                    return new RuntimeException("Rol de usuario autenticado no encontrado");
                })
                .getAuthority());

        logger.debug("Usuario autenticado realizando registro - Rol: {}", registeringUserRole);

        //Un ADMIN puede registrar cualquier rol, incluido otro ADMIN
        //Los demás roles solo pueden registrar roles con un nivel de jerarquía inferior
        if (registeringUserRole != UserRole.ADMIN && registeringUserRole.getHierarchyLevel() <= request.role().getHierarchyLevel()) {
            logger.warn("Intento de registro no autorizado - Rol registrador: {}, Rol solicitado: {}",
                    registeringUserRole, request.role());
            throw new UnauthorizedRegistrationException("El usuario con rol " + registeringUserRole.name() + " no está autorizado para registrar un usuario con rol " + request.role().name());
        }

        logger.debug("Autorización de registro validada, verificando duplicados");

        //Validamos que el email y el número de documento no existan
        if(userRepository.existsByEmail(request.email())){
            logger.warn("Intento de registro con email duplicado: {}", request.email());
            throw  new ResourceAlreadyExistsException("El email ya se encuentra registrado.");
        }

        if(userRepository.existsByDocumentNumber(request.documentNumber())){
            logger.warn("Intento de registro con número de documento duplicado: {}", request.documentNumber());
            throw new ResourceAlreadyExistsException("El número de documento ya se encuentra registrado.");
        }

        logger.debug("Validaciones de duplicados exitosas, creando nuevo usuario");
        //Mapear el DTO a la entidad User
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        //Mapear la información del DTO al objeto embebido PersonalInformation
        PersonalInformation personalInformation = getPersonalInformation(request);

        user.setPersonalInformation(personalInformation);

        //Guardamos el usuario en la base de datos
        user = userRepository.save(user);
        UserResponse response = getUserResponse(user);
        logger.info("Usuario registrado exitosamente por administrador - ID: {}, Email: {}, Rol: {}",
                response.idUser(), response.email(), response.role());
        return response;
    }

    @Override
    public UserResponse publicRegisterUser(UserRequest request) {
        logger.info("Iniciando registro público de usuario - Email: {}, Documento: {}",
                request.email(), request.documentNumber());

        if(userRepository.existsByEmail(request.email())){
            throw new ResourceAlreadyExistsException("El email ya se encuentra registrado.");
        }

        if(userRepository.existsByDocumentNumber(request.documentNumber())){
            throw new ResourceAlreadyExistsException("El número de documento ya se encuentra registrado.");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.MEMBER);

        PersonalInformation personalInformation = getPersonalInformation(request);
        user.setPersonalInformation(personalInformation);

        // 🔥 ASIGNAR UBICACIÓN PRINCIPAL si se proporciona durante el registro
        if (request.mainLocationId() != null) {
            Location mainLocation = locationRepository.findById(request.mainLocationId())
                    .orElseThrow(() -> {
                        logger.error("Ubicación no encontrada con ID: {}", request.mainLocationId());
                        return new LocationNotFoundException("La ubicación seleccionada no existe");
                    });
            // Pasar el objeto Location completo, el setter extrae el ID internamente
            user.setMainLocation(mainLocation);
            logger.info("Ubicación principal asignada al usuario - Location ID: {}, Nombre: {}",
                    mainLocation.getIdLocation(), mainLocation.getName());
        } else {
            logger.warn("Usuario registrado sin ubicación principal - Email: {}", request.email());
        }

        // 🔥 Guardar usuario con ubicación asignada
        User savedUser = userRepository.save(user);

        // 🚀 Retornar la respuesta con los datos persistidos
        UserResponse response = getUserResponse(savedUser);

        String locationInfo = savedUser.getMainLocation() != null
            ? "ID: " + savedUser.getMainLocation()
            : "No asignada";

        logger.info("Usuario registrado exitosamente de forma pública - ID: {}, Email: {}, Rol: {}, MainLocation: {}",
                response.idUser(), response.email(), response.role(), locationInfo);

        return response;
    }


    /**
     * Extrae la información personal de un DTO de solicitud.
     * @param request Objeto que contiene los datos personales del usuario.
     * @return Un nuevo objeto PersonalInformation
     */
    private static PersonalInformation getPersonalInformation(UserRequest request) {
        logger.debug("Mapeando información personal del usuario - Nombre: {}, Apellido: {}",
                request.firstName(), request.lastName());

        PersonalInformation personalInformation = new PersonalInformation();
        personalInformation.setFirstName(request.firstName());
        personalInformation.setLastName(request.lastName());
        personalInformation.setDocumentType(request.documentType());
        personalInformation.setDocumentNumber(request.documentNumber());
        personalInformation.setBirthDate(request.birthDate());
        personalInformation.setMedicalConditions(request.medicalConditions());
        personalInformation.setEmergencyContactName(request.emergencyContactPhone());
        personalInformation.setPhoneNumber(request.phoneNumber());

        logger.debug("Información personal mapeada exitosamente");
        return personalInformation;
    }

    /**
     * Método para actualizar un usuario
     * @param idUser ID del usuario a actualizar
     * @param request objeto que contiene los datos actualizados del usuario
     * @return
     */
    @Override
    public UserResponse updateUser(Long idUser, UserUpdateRequest request) {
        logger.info("Iniciando actualización de usuario - ID: {}", idUser);
        logger.debug("Campos a actualizar - Nombre: {}, Apellido: {}, Email: {}, Documento: {}",
                request.firstName(), request.lastName(), request.email(), request.documentNumber());

        // Buscar el usuario por su ID. Si no existe, lanzar una excepción.
        User existingUser = userRepository.findById(idUser)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para actualización con ID: {}", idUser);
                    return new UserNotFoundException("Usuario no encontrado con el ID: " + idUser);
                });

        logger.debug("Usuario encontrado para actualización: {} (ID: {})",
                existingUser.getPersonalInformation().getFirstName(), idUser);

        // Actualizar solo los campos que no son nulos en la solicitud
        if(request.firstName() != null) {
            existingUser.getPersonalInformation().setFirstName(request.firstName());
            logger.debug("Nombre actualizado: {}", request.firstName());
        }
        if(request.lastName() != null) {
            existingUser.getPersonalInformation().setLastName(request.lastName());
            logger.debug("Apellido actualizado: {}", request.lastName());
        }
        if(request.email() != null && !existingUser.getEmail().equals(request.email())) {
            if(userRepository.existsByEmail(request.email())){
                logger.warn("Intento de actualización con email duplicado: {}", request.email());
                throw new ResourceAlreadyExistsException("El nuevo email ya se encuentra registrado por otro usuario.");
            }
            existingUser.setEmail(request.email());
            logger.debug("Email actualizado: {}", request.email());
        }
        if(request.documentNumber() != null && !existingUser.getPersonalInformation().getDocumentNumber().equals(request.documentNumber())) {
            if(userRepository.existsByDocumentNumber(request.documentNumber())){
                logger.warn("Intento de actualización con número de documento duplicado: {}", request.documentNumber());
                throw new ResourceAlreadyExistsException("El nuevo número de documento ya se encuentra registrado por otro usuario.");
            }
            existingUser.getPersonalInformation().setDocumentNumber(request.documentNumber());
            logger.debug("Número de documento actualizado: {}", request.documentNumber());
        }
        if(request.documentType() != null) {
            existingUser.getPersonalInformation().setDocumentType(request.documentType());
            logger.debug("Tipo de documento actualizado: {}", request.documentType());
        }
        if(request.phoneNumber() != null) {
            existingUser.getPersonalInformation().setPhoneNumber(request.phoneNumber());
            logger.debug("Número de teléfono actualizado: {}", request.phoneNumber());
        }
        if(request.birthDate() != null) {
            existingUser.getPersonalInformation().setBirthDate(request.birthDate());
            logger.debug("Fecha de nacimiento actualizada: {}", request.birthDate());
        }
        if(request.emergencyContactPhone() != null) {
            existingUser.getPersonalInformation().setEmergencyContactName(request.emergencyContactPhone());
            logger.debug("Teléfono de contacto de emergencia actualizado: {}", request.emergencyContactPhone());
        }
        if(request.medicalConditions() != null) {
            existingUser.getPersonalInformation().setMedicalConditions(request.medicalConditions());
            logger.debug("Condiciones médicas actualizadas");
        }

        logger.debug("Guardando usuario actualizado en la base de datos");
        UserResponse response = getUserResponse(existingUser);
        logger.info("Usuario actualizado exitosamente - ID: {}, Email: {}", idUser, response.email());
        return response;
    }

    private UserResponse getUserResponse(User existingUser) {
        logger.debug("Guardando usuario en la base de datos - Email: {}", existingUser.getEmail());
        User updatedUser = userRepository.save(existingUser);
        logger.debug("Usuario guardado exitosamente con ID: {}", updatedUser.getIdUser());

        // ✅ CORREGIDO: Usar el método fromUser en lugar del constructor directo
        return UserResponse.fromUser(updatedUser, null);
    }

    /**
     * Actualiza la información personal del usuario existente con los datos del request
     * @param request Objeto que contiene los datos actualizados del usuario
     * @param existingUser Usuario existente en la base de datos
     * @return Objeto PersonalInformation actualizado
     */
    private static PersonalInformation getPersonalInformation(UserRequest request, User existingUser) {
        logger.debug("Actualizando información personal del usuario existente - ID: {}", existingUser.getIdUser());

        PersonalInformation personalInfo = existingUser.getPersonalInformation();
        personalInfo.setFirstName(request.firstName());
        personalInfo.setLastName(request.lastName());
        personalInfo.setDocumentType(request.documentType());
        personalInfo.setDocumentNumber(request.documentNumber());
        personalInfo.setBirthDate(request.birthDate());
        personalInfo.setPhoneNumber(request.phoneNumber());
        personalInfo.setMedicalConditions(request.medicalConditions());
        personalInfo.setEmergencyContactName(request.emergencyContactPhone());

        logger.debug("Información personal del usuario existente actualizada exitosamente");
        return personalInfo;
    }

    /**
     * Este método hace un borrado lógico del usuario (cambia el estado a false)
     * @param idUser ID del usuario a eliminar
     */
    @Override
    public void deleteUser(Long idUser) {
        logger.info("Iniciando eliminación lógica de usuario - ID: {}", idUser);

        User user = userRepository.findById(idUser)
                .orElseThrow(()-> {
                    logger.error("Usuario no encontrado para eliminación con ID: {}", idUser);
                    return new UserNotFoundException("Usuario no encontrado con el ID: " + idUser);
                });

        logger.debug("Usuario encontrado para eliminación: {} (ID: {})",
                user.getPersonalInformation().getFirstName(), idUser);

        user.setActive(false);
        userRepository.save(user);
        logger.info("Usuario desactivado exitosamente - ID: {}, Email: {}", idUser, user.getEmail());
    }

    /**
     * Método que obtiene a un usuario dado su id
     * @param idUser ID del usuario a buscar
     * @return UserResponse, objeto que contiene la información del usuario
     */
    @Override
    public UserResponse getUserById(Long idUser) {
        logger.debug("Consultando usuario por ID: {}", idUser);

        User user = userRepository.findById(idUser)
                .orElseThrow( () -> {
                    logger.error("Usuario no encontrado con ID: {}", idUser);
                    return new UserNotFoundException("El id ingresado no existe");
                });

        logger.debug("Usuario encontrado por ID: {} (ID: {})",
                user.getPersonalInformation().getFirstName(), idUser);

        // ✅ MEJORADO: Primero intentar usar el campo directo membershipType
        String membershipType = null;

        // Opción 1: Usar el campo directo si está disponible
        if (user.getMembershipType() != null) {
            membershipType = user.getMembershipType().name();
            logger.info("✅ [getUserById] MembershipType obtenido del campo directo: {}", membershipType);
        } else {
            // Opción 2: Si no está disponible, consultar desde el servicio de membresía
            try {
                logger.debug("🔍 [getUserById] Campo membershipType null, consultando desde servicio de membresía");
                MembershipResponse membership = membershipService.getMembershipByUserId(idUser);

                if (membership != null && MembershipStatus.ACTIVE.equals(membership.status())) {
                    membershipType = membership.membershipTypeName().name();
                    logger.info("✅ [getUserById] Usuario {} tiene membresía activa: {}", idUser, membershipType);
                }
            } catch (Exception e) {
                // Usuario no tiene membresía activa
                logger.info("ℹ️ [getUserById] Usuario {} no tiene membresía activa: {}", idUser, e.getMessage());
                membershipType = null;
            }
        }

        // ✅ USAR EL MÉTODO fromUser QUE INCLUYE MEMBERSHIPTYPE
        UserResponse response = UserResponse.fromUser(user, membershipType);

        // ✅ Log final del objeto que se va a devolver
        logger.info("📦 [getUserById] Respuesta generada - idUser: {}, id: {}, membershipType: {}, email: {}",
                response.idUser(), response.id(), response.membershipType(), response.email());

        return response;
    }

    /**
     * Este método me devuelve todos los usuarios activos
     * @return
     */
    @Override
    public List<UserResponse> getAllUsers() {
        logger.debug("Consultando todos los usuarios activos");

        List<User> users = userRepository.findByIsActiveTrue();
        logger.debug("Se encontraron {} usuarios activos", users.size());

        // ✅ CORREGIDO: Usar fromUser en lugar del constructor directo
        return users.stream()
                .map(user -> UserResponse.fromUser(user, null))
                .toList();
    }

    /**
     * Método para obtener un usuario dado su correo electrónico
     * @param email correo electrónico del usuario a buscar
     * @return
     */
    @Override
    public UserResponse getUserByEmail(String email) {
        logger.debug("Consultando usuario por email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow( () -> {
                    logger.error("Usuario no encontrado con email: {}", email);
                    return new UserNotFoundException("El email ingresado no existe");
                });

        logger.debug("Usuario encontrado por email: {} (ID: {})",
                user.getPersonalInformation().getFirstName(), user.getIdUser());

        // ✅ CORREGIDO: Usar fromUser en lugar del constructor directo
        return UserResponse.fromUser(user, null);
    }

    /**
     * Método para obtener un usuario dado su número de documento
     * @param documentNumber número de documento del usuario a buscar
     * @return
     */
    @Override
    public UserResponse getUserByDocumentNumber(String documentNumber) {
        logger.debug("Consultando usuario por número de documento: {}", documentNumber);

        User user = userRepository.findByDocumentNumber(documentNumber)
                .orElseThrow( () -> {
                    logger.error("Usuario no encontrado con número de documento: {}", documentNumber);
                    return new UserNotFoundException("El número de documento ingresado no existe");
                });

        logger.debug("Usuario encontrado por documento: {} (ID: {})",
                user.getPersonalInformation().getFirstName(), user.getIdUser());

        // ✅ CORREGIDO: Usar fromUser en lugar del constructor directo
        return UserResponse.fromUser(user, null);
    }

    @Override
    public boolean validateCredentials(LoginRequest request) {
        try {
            // Buscar el usuario por email
            User user = userRepository.findByEmail(request.email())
                    .orElse(null);

            if (user == null) {
                logger.warn("Usuario no encontrado: {}", request.email());
                return false;
            }

            // Verificar si el usuario está activo
            if (!user.isActive()) {
                logger.warn("Usuario inactivo: {}", request.email());
                return false;
            }

            // Comparar la contraseña usando el passwordEncoder
            boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());

            if (passwordMatches) {
                logger.info("Credenciales válidas para usuario: {}", request.email());
            } else {
                logger.warn("Contraseña incorrecta para usuario: {}", request.email());
            }

            return passwordMatches;

        } catch (Exception e) {
            logger.error("Error validando credenciales para usuario {}: {}", request.email(), e.getMessage());
            return false;
        }
    }

    @Override
    public String generateOTP(String email) {
        return "";
    }

    @Override
    public void sendOTPEmail(String email, String otp) {

    }

    @Override
    public boolean validateOTP(String email, String otp) {
        return false;
    }

    @Override
    public String loginAfterOTP(String email) {
        return "";
    }

}
