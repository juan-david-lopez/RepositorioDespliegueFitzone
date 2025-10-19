package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.LoyaltyReward;
import co.edu.uniquindio.FitZone.model.enums.RewardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad LoyaltyReward
 */
@Repository
public interface LoyaltyRewardRepository extends JpaRepository<LoyaltyReward, Long> {

    /**
     * Encuentra todas las recompensas activas
     */
    List<LoyaltyReward> findByIsActiveTrue();

    /**
     * Encuentra recompensas por tipo
     */
    List<LoyaltyReward> findByRewardTypeAndIsActiveTrue(RewardType rewardType);

    /**
     * Encuentra recompensas que el usuario puede canjear según sus puntos
     */
    @Query("SELECT lr FROM LoyaltyReward lr WHERE lr.pointsCost <= :availablePoints AND lr.isActive = true ORDER BY lr.pointsCost ASC")
    List<LoyaltyReward> findAffordableRewards(@Param("availablePoints") Integer availablePoints);

    /**
     * Encuentra recompensas por tier mínimo requerido
     */
    @Query("SELECT lr FROM LoyaltyReward lr WHERE lr.minimumTierRequired <= :userTier AND lr.isActive = true")
    List<LoyaltyReward> findByMinimumTierRequired(@Param("userTier") String userTier);

    /**
     * Encuentra recompensas que el usuario puede canjear según puntos y tier
     */
    @Query("SELECT lr FROM LoyaltyReward lr WHERE lr.pointsCost <= :availablePoints " +
           "AND lr.minimumTierRequired <= :userTier AND lr.isActive = true " +
           "ORDER BY lr.pointsCost ASC")
    List<LoyaltyReward> findAvailableRewards(@Param("availablePoints") Integer availablePoints,
                                           @Param("userTier") String userTier);

    /**
     * Cuenta recompensas activas
     */
    long countByIsActiveTrue();
}
