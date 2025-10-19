package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.LoyaltyProfile;
import co.edu.uniquindio.FitZone.model.enums.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad LoyaltyProfile
 */
@Repository
public interface LoyaltyProfileRepository extends JpaRepository<LoyaltyProfile, Long> {

    /**
     * Encuentra el perfil de fidelización por ID de usuario
     */
    Optional<LoyaltyProfile> findByUserId(Long userId);

    /**
     * Encuentra perfiles activos
     */
    @SuppressWarnings("unused")
    List<LoyaltyProfile> findByIsActiveTrue();

    /**
     * Encuentra perfiles por tier
     */
    @SuppressWarnings("unused")
    List<LoyaltyProfile> findByCurrentTier(LoyaltyTier currentTier);

    /**
     * Encuentra el top usuario por puntos totales
     */
    @SuppressWarnings("unused")
    @Query("SELECT lp FROM LoyaltyProfile lp WHERE lp.isActive = true ORDER BY lp.totalPoints DESC")
    List<LoyaltyProfile> findTopUsersByPoints();

    /**
     * Encuentra usuarios con puntos disponibles mayores a un valor
     */
    @SuppressWarnings("unused")
    List<LoyaltyProfile> findByAvailablePointsGreaterThanEqual(Integer minPoints);

    /**
     * Obtiene estadísticas de puntos por tier
     */
    @SuppressWarnings("unused")
    @Query("SELECT lp.currentTier, AVG(lp.totalPoints) FROM LoyaltyProfile lp WHERE lp.isActive = true GROUP BY lp.currentTier")
    List<Object[]> getPointsStatsByTier();

    /**
     * Verifica si existe un perfil para un usuario
     */
    @SuppressWarnings("unused")
    boolean existsByUserId(Long userId);
}
