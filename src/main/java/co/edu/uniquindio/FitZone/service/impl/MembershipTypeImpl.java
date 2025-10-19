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

import java.util.ArrayList;
import java.util.List;

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

        if(membershipTypeRepository.existsByName(membershipTypeRequest.name())){
            logger.warn("Intento de crear tipo de membresía con nombre duplicado: {}", membershipTypeRequest.name());
            throw new ResourceAlreadyExistsException("El tipo de membresía ya existe");
        }

        MembershipType membershipType = getMembershipType(membershipTypeRequest);
        MembershipType savedMembershipType = membershipTypeRepository.save(membershipType);

        logger.info("Tipo de membresía creado exitosamente - ID: {}, Nombre: {}",
            savedMembershipType.getIdMembershipType(), savedMembershipType.getName());

        return mapToResponse(savedMembershipType);
    }

    @Override
    public MembershipTypeResponse updateMembershipType(Long id, MembershipTypeRequest request) {
        logger.info("Iniciando actualización de tipo de membresía con ID: {}", id);

        MembershipType membershipType = membershipTypeRepository.findById(id)
                .orElseThrow(() -> new MembershipTypeNotFoundException("Tipo de membresía no encontrado"));

        if(!membershipType.getName().equals(request.name()) && membershipTypeRepository.existsByName(request.name())){
           throw new ResourceAlreadyExistsException("El tipo de membresía ya existe");
        }

        membershipType.setName(request.name());
        membershipType.setDescription(request.description());
        membershipType.setMonthlyPrice(request.monthlyPrice());
        membershipType.setAccessToAllLocation(request.accessToAllLocation());
        membershipType.setGroupClassesSessionsIncluded(request.groupClassesSessionsIncluded());
        membershipType.setPersonalTrainingIncluded(request.personalTrainingIncluded());
        membershipType.setSpecializedClassesIncluded(request.specializedClassesIncluded());

        MembershipType updatedMembershipType = membershipTypeRepository.save(membershipType);
        logger.info("Tipo de membresía actualizado exitosamente - ID: {}, Nombre: {}", 
            updatedMembershipType.getIdMembershipType(), updatedMembershipType.getName());

        return mapToResponse(updatedMembershipType);
    }

    @Override
    public MembershipTypeResponse getMembershipTypeById(Long id) {
        logger.debug("Consultando tipo de membresía por ID: {}", id);

        MembershipType membershipType = membershipTypeRepository.findById(id)
                .orElseThrow(() -> new MembershipTypeNotFoundException("Tipo de membresía no encontrado"));

        return mapToResponse(membershipType);
    }

    @Override
    public MembershipTypeResponse getMembershipTypeByName(MembershipTypeName name) {
        logger.debug("Consultando tipo de membresía por nombre: {}", name);

        MembershipType membershipType = membershipTypeRepository.findByName(name)
                .orElseThrow(() -> new MembershipTypeNotFoundException("Tipo de membresía no encontrado"));

        return mapToResponse(membershipType);
    }

    @Override
    public List<MembershipTypeResponse> getAllMembershipTypes() {
        logger.debug("Consultando todos los tipos de membresía");

        List<MembershipType> membershipTypes = new ArrayList<>();
        membershipTypeRepository.findAll().forEach(membershipTypes::add);

        logger.debug("Tipos de membresía encontrados: {} registros", membershipTypes.size());

        return membershipTypes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MembershipTypeResponse> getMembershipTypes() {
        logger.debug("Consultando todos los tipos de membresía (método alternativo)");
        return getAllMembershipTypes();
    }

    private MembershipType getMembershipType(MembershipTypeRequest membershipTypeRequest) {
        MembershipType membershipType = new MembershipType();
        membershipType.setName(membershipTypeRequest.name());
        membershipType.setDescription(membershipTypeRequest.description());
        membershipType.setMonthlyPrice(membershipTypeRequest.monthlyPrice());
        membershipType.setAccessToAllLocation(membershipTypeRequest.accessToAllLocation());
        membershipType.setGroupClassesSessionsIncluded(membershipTypeRequest.groupClassesSessionsIncluded());
        membershipType.setPersonalTrainingIncluded(membershipTypeRequest.personalTrainingIncluded());
        membershipType.setSpecializedClassesIncluded(membershipTypeRequest.specializedClassesIncluded());
        return membershipType;
    }

    private MembershipTypeResponse mapToResponse(MembershipType membershipType) {
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
}