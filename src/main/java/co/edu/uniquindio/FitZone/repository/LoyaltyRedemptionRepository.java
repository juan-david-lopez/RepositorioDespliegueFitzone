package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.LoyaltyRedemption;
import co.edu.uniquindio.FitZone.model.enums.RedemptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad LoyaltyRedemption
 */
@Repository
public interface LoyaltyRedemptionRepository extends JpaRepository<LoyaltyRedemption, Long> {

    /**
     * Encuentra redempciones por usuario
     */
    List<LoyaltyRedemption> findByUserIdOrderByRedemptionDateDesc(Long userId);

    /**
     * Encuentra redempciones por perfil de fidelización
     */
    List<LoyaltyRedemption> findByLoyaltyProfileIdOrderByRedemptionDateDesc(Long loyaltyProfileId);

    /**
     * Encuentra redempciones por estado
     */
    List<LoyaltyRedemption> findByStatus(RedemptionStatus status);

    /**
     * Encuentra redempción por código
     */
    Optional<LoyaltyRedemption> findByRedemptionCode(String redemptionCode);

    /**
     * Encuentra redempciones por recompensa
     */
    List<LoyaltyRedemption> findByLoyaltyRewardId(Long loyaltyRewardId);

    /**
     * Encuentra redempciones activas (no expiradas)
     */
    @Query("SELECT lr FROM LoyaltyRedemption lr WHERE lr.expirationDate > :currentDate " +
           "AND lr.status = 'ACTIVE' ORDER BY lr.redemptionDate DESC")
    List<LoyaltyRedemption> findActiveRedemptions(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Encuentra redempciones por usuario en un rango de fechas
     */
    @Query("SELECT lr FROM LoyaltyRedemption lr WHERE lr.userId = :userId " +
           "AND lr.redemptionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY lr.redemptionDate DESC")
    List<LoyaltyRedemption> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Cuenta total de redempciones por usuario
     */
    long countByUserId(Long userId);

    /**
     * Suma total de puntos gastados por usuario
     */
    @Query("SELECT COALESCE(SUM(lr.pointsSpent), 0) FROM LoyaltyRedemption lr WHERE lr.userId = :userId")
    Integer sumPointsSpentByUserId(@Param("userId") Long userId);

    /**
     * Encuentra redempciones próximas a expirar
     */
    @Query("SELECT lr FROM LoyaltyRedemption lr WHERE lr.expirationDate BETWEEN :now AND :futureDate " +
           "AND lr.status = 'ACTIVE'")
    List<LoyaltyRedemption> findExpiringRedemptions(@Param("now") LocalDateTime now,
                                                  @Param("futureDate") LocalDateTime futureDate);

    @Query("SELECT lr FROM LoyaltyRedemption lr WHERE lr.expirationDate <= :currentDate AND lr.status != 'EXPIRED'")
    List<LoyaltyRedemption> findExpiredRedemptions(@Param("currentDate") LocalDateTime currentDate);
}
