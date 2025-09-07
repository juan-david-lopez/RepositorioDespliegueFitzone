package co.edu.uniquindio.FitZone.model.enums;

import lombok.Getter;

/**
 * ENUM - Representa los roles de usuario en el sistema
 */
@Getter
public enum UserRole {

    MEMBER(1),
    INSTRUCTOR(2),
    RECEPTIONIST(3),
    ADMIN(4);

    private final int hierarchyLevel;

    UserRole(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public boolean canRegister(UserRole otherRole){
        return this.hierarchyLevel >= otherRole.getHierarchyLevel();
    }

}
