package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.MembershipTypeRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipTypeResponse;
import co.edu.uniquindio.FitZone.exception.MembershipTypeNotFoundException;
import co.edu.uniquindio.FitZone.exception.ResourceAlreadyExistsException;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Implementación del servicio para gestionar tipos de membresía.
 * Proporciona métodos para crear, actualizar, consultar por ID o nombre, y listar todos los tipos de membresía.
 * Utiliza un repositorio para interactuar con la base de datos y maneja excepciones específicas.
 * Incluye logging detallado para facilitar el seguimiento de las operaciones.
 */
@Service
public class MembershipTypeImpl implements IMembershipTypeService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipTypeImpl.class);

    private final MembershipTypeRepository membershipTypeRepository;

    public MembershipTypeImpl(MembershipTypeRepository membershipTypeRepository) {
        this.membershipTypeRepository = membershipTypeRepository;
    }

    @Override
    public MembershipTypeResponse createMembershipType(MembershipTypeRequest membershipTypeRequest) {
        logger.info("Iniciando creación de tipo de membresía: {}", membershipTypeRequest.name());
        logger.debug("Datos del tipo de membresía - Descripción: {}, Precio mensual: {}, Acceso a todas las sedes: {}", 
            membershipTypeRequest.description(), membershipTypeRequest.monthlyPrice(), membershipTypeRequest.accessToAllLocation());

        if(membershipTypeRepository.existsByName(membershipTypeRequest.name())){
            logger.warn("Intento de crear tipo de membresía con nombre duplicado: {}", membershipTypeRequest.name());
            throw new ResourceAlreadyExistsException("El tipo de membresía ya existe");
        }

        logger.debug("Nombre único validado, procediendo a crear el tipo de membresía");
        MembershipType membershipType = getMembershipType(membershipTypeRequest);

        logger.debug("Guardando tipo de membresía en la base de datos");
        MembershipType savedMembershipType = membershipTypeRepository.save(membershipType);
        logger.info("Tipo de membresía creado exitosamente - ID: {}, Nombre: {}", 
            savedMembershipType.getIdMembershipType(), savedMembershipType.getName());

        return new MembershipTypeResponse(
                savedMembershipType.getIdMembershipType(),
                savedMembershipType.getName(),
                savedMembershipType.getDescription(),
                savedMembershipType.getMonthlyPrice(),
                savedMembershipType.getAccessToAllLocation(),
                savedMembershipType.getGroupClassesSessionsIncluded(),
                savedMembershipType.getPersonalTrainingIncluded(),
                savedMembershipType.getSpecializedClassesIncluded()
        );
    }


    @Override
    public MembershipTypeResponse updateMembershipType(Long id, MembershipTypeRequest request) {
        logger.info("Iniciando actualización de tipo de membresía con ID: {}", id);
        logger.debug("Nuevos datos - Nombre: {}, Descripción: {}, Precio mensual: {}", 
            request.name(), request.description(), request.monthlyPrice());

        MembershipType membershipType = membershipTypeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tipo de membresía no encontrado para actualización con ID: {}", id);
                    return new MembershipTypeNotFoundException("Tipo de membresía no encontrado");
                });

        logger.debug("Tipo de membresía encontrado: {} (ID: {})", membershipType.getName(), id);

       if(!membershipType.getName().equals(request.name()) && membershipTypeRepository.existsByName(request.name())){
           logger.warn("Intento de actualización con nombre duplicado: {}", request.name());
           throw new ResourceAlreadyExistsException("El tipo de membresía ya existe");
       }

        logger.debug("Validación de nombre único exitosa, actualizando datos del tipo de membresía");
        membershipType.setName(request.name());
        membershipType.setDescription(request.description());
        membershipType.setMonthlyPrice(request.monthlyPrice());
        membershipType.setAccessToAllLocation(request.accessToAllLocation());
        membershipType.setGroupClassesSessionsIncluded(request.groupClassesSessionsIncluded());
        membershipType.setPersonalTrainingIncluded(request.personalTrainingIncluded());
        membershipType.setSpecializedClassesIncluded(request.specializedClassesIncluded());

        logger.debug("Guardando tipo de membresía actualizado en la base de datos");
        MembershipType updatedMembershipType = membershipTypeRepository.save(membershipType);
        logger.info("Tipo de membresía actualizado exitosamente - ID: {}, Nombre: {}", 
            updatedMembershipType.getIdMembershipType(), updatedMembershipType.getName());

        return new MembershipTypeResponse(
                updatedMembershipType.getIdMembershipType(),
                updatedMembershipType.getName(),
                updatedMembershipType.getDescription(),
                updatedMembershipType.getMonthlyPrice(),
                updatedMembershipType.getAccessToAllLocation(),
                updatedMembershipType.getGroupClassesSessionsIncluded(),
                updatedMembershipType.getPersonalTrainingIncluded(),
                updatedMembershipType.getSpecializedClassesIncluded()
        );
    }

    @Override
    public MembershipTypeResponse getMembershipTypeById(Long id) {
        logger.debug("Consultando tipo de membresía por ID: {}", id);

        MembershipType membershipType = membershipTypeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tipo de membresía no encontrado con ID: {}", id);
                    return new MembershipTypeNotFoundException("Tipo de membresía no encontrado");
                });

        logger.debug("Tipo de membresía encontrado: {} (ID: {})", membershipType.getName(), id);
        return new MembershipTypeResponse(
                membershipType.getIdMembershipType(),
                membershipType.getName(),
                membershipType.getDescription(),
                membershipType.getMonthlyPrice(),
                membershipType.getAccessToAllLocation(),
                membershipType.getGroupClassesSessionsIncluded(),
                membershipType.getPersonalTrainingIncluded(),
                membershipType.getSpecializedClassesIncluded()
        );
    }

    @Override
    public MembershipTypeResponse getMembershipTypeByName(MembershipTypeName name) {
        logger.debug("Consultando tipo de membresía por nombre: {}", name);

        MembershipType membershipType = membershipTypeRepository.findByName(name)
                .orElseThrow(() -> {
                    logger.error("Tipo de membresía no encontrado con nombre: {}", name);
                    return new MembershipTypeNotFoundException("Tipo de membresía no encontrado");
                });

        logger.debug("Tipo de membresía encontrado por nombre: {} (ID: {})", name, membershipType.getIdMembershipType());
        return new MembershipTypeResponse(
                membershipType.getIdMembershipType(),
                membershipType.getName(),
                membershipType.getDescription(),
                membershipType.getMonthlyPrice(),
                membershipType.getAccessToAllLocation(),
                membershipType.getGroupClassesSessionsIncluded(),
                membershipType.getPersonalTrainingIncluded(),
                membershipType.getSpecializedClassesIncluded()
        );
    }

    @Override
    public List<MembershipTypeResponse> getMembershipTypes() {
        logger.debug("Consultando todos los tipos de membresía");

        List<MembershipTypeResponse> membershipTypes = StreamSupport
                .stream(membershipTypeRepository.findAll().spliterator(), false)
                .map(m -> new MembershipTypeResponse(
                        m.getIdMembershipType(),
                        m.getName(),
                        m.getDescription(),
                        m.getMonthlyPrice(),
                        m.getAccessToAllLocation(),
                        m.getGroupClassesSessionsIncluded(),
                        m.getPersonalTrainingIncluded(),
                        m.getSpecializedClassesIncluded()
                ))
                .toList();

        logger.debug("Se encontraron {} tipos de membresía", membershipTypes.size());
        return membershipTypes;
    }

    /**
     * Método privado para mapear un MembershipTypeRequest a una entidad MembershipType
     * @param membershipTypeRequest DTO de solicitud que contiene los datos del tipo de membresía
     * @return Entidad MembershipType mapeada
     */
    private static MembershipType getMembershipType(MembershipTypeRequest membershipTypeRequest) {
        logger.debug("Mapeando MembershipTypeRequest a entidad MembershipType - Nombre: {}", membershipTypeRequest.name());
        
        MembershipType membershipType = new MembershipType();
        membershipType.setName(membershipTypeRequest.name());
        membershipType.setDescription(membershipTypeRequest.description());
        membershipType.setMonthlyPrice(membershipTypeRequest.monthlyPrice());
        membershipType.setAccessToAllLocation(membershipTypeRequest.accessToAllLocation());
        membershipType.setGroupClassesSessionsIncluded(membershipTypeRequest.groupClassesSessionsIncluded());
        membershipType.setPersonalTrainingIncluded(membershipTypeRequest.personalTrainingIncluded());
        membershipType.setSpecializedClassesIncluded(membershipTypeRequest.specializedClassesIncluded());
        
        logger.debug("Mapeo completado para el tipo de membresía: {}", membershipTypeRequest.name());
        return membershipType;
    }
}
