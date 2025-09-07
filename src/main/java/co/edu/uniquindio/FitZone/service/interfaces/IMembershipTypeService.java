package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.MembershipTypeRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipTypeResponse;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.util.List;

/**
 * Define los contratos del servicio de tipo de membresía.
 * Este servicio maneja la lógica de negocio relacionada con los tipos de membresía.
 * Aquí se definen métodos para crear, actualizar, obtener y listar tipos de membresía.
 */
public interface IMembershipTypeService {

    /**
     * Crea un nuevo tipo de membresía en el sistema.
     * Este método recibe un objeto MembershipTypeRequest que contiene la información necesaria para crear un tipo de membresía.
     * @param membershipTypeRequest objeto que contiene los datos del tipo de membresía a crear
     * @return MembershipTypeResponse objeto que representa la respuesta de la creación del tipo de membresía
     */
    MembershipTypeResponse createMembershipType(MembershipTypeRequest membershipTypeRequest);

    /**
     * Actualiza un tipo de membresía existente en el sistema.
     * Este método recibe el ID del tipo de membresía a actualizar y un objeto MembershipTypeRequest que contiene los datos actualizados.
     * @param id ID del tipo de membresía a actualizar
     * @param membershipTypeRequest objeto que contiene los datos actualizados del tipo de membresía
     * @return MembershipTypeResponse objeto que contiene la información actualizada del tipo de membresía
     */
    MembershipTypeResponse updateMembershipType(Long id, MembershipTypeRequest membershipTypeRequest);

    /**
     * Obtiene un tipo de membresía por su ID.
     * Este método recibe el ID del tipo de membresía y devuelve un objeto MembershipTypeResponse
     * @param id ID del tipo de membresía a buscar
     * @return MembershipTypeResponse objeto que representa al tipo de membresía encontrado
     */
    MembershipTypeResponse getMembershipTypeById(Long id);

    /**
     * Obtiene un tipo de membresía por su nombre.
     * Este método recibe el nombre del tipo de membresía y devuelve un objeto MembershipTypeResponse
     * @param name nombre del tipo de membresía a buscar
     * @return MembershipTypeResponse objeto que representa al tipo de membresía encontrado
     */
    MembershipTypeResponse getMembershipTypeByName(MembershipTypeName name);

    /**
     * Obtiene una lista de todos los tipos de membresía en el sistema.
     * Este método devuelve una lista de objetos MembershipTypeResponse que representan a todos los tipos de membresía.
     * @return List<MembershipTypeResponse> lista de tipos de membresía
     */
    List<MembershipTypeResponse> getMembershipTypes();

}
