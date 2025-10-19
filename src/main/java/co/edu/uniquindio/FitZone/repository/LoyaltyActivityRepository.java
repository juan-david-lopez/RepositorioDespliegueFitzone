package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.LoyaltyActivity;
import co.edu.uniquindio.FitZone.model.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad LoyaltyActivity
 */
@Repository
public interface LoyaltyActivityRepository extends JpaRepository<LoyaltyActivity, Long> {

    /**
     * Encuentra actividades por perfil de fidelización
     */
    List<LoyaltyActivity> findByLoyaltyProfileIdOrderByActivityDateDesc(Long loyaltyProfileId);

    /**
     * Encuentra actividades por usuario
     */
    List<LoyaltyActivity> findByUserIdOrderByActivityDateDesc(Long userId);

    /**
     * Encuentra actividades por tipo
     */
    List<LoyaltyActivity> findByActivityType(ActivityType activityType);

    /**
     * Encuentra actividades no canceladas ni expiradas
     */
    List<LoyaltyActivity> findByIsCancelledFalseAndIsExpiredFalse();

    /**
     * Encuentra actividades por usuario en un rango de fechas
     */
    @Query("SELECT la FROM LoyaltyActivity la WHERE la.userId = :userId " +
           "AND la.activityDate BETWEEN :startDate AND :endDate " +
           "ORDER BY la.activityDate DESC")
    List<LoyaltyActivity> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Suma total de puntos ganados por usuario
     */
    @Query("SELECT COALESCE(SUM(la.pointsEarned), 0) FROM LoyaltyActivity la " +
           "WHERE la.userId = :userId AND la.isCancelled = false")
    Integer sumPointsByUserId(@Param("userId") Long userId);

    /**
     * Encuentra actividades de bonificación
     */
    List<LoyaltyActivity> findByIsBonusActivityTrue();

    /**
     * Cuenta actividades por usuario y tipo
     */
    long countByUserIdAndActivityType(Long userId, ActivityType activityType);

    @Query("SELECT la FROM LoyaltyActivity la WHERE la.expirationDate <= :currentDate AND la.isExpired = false")
    List<LoyaltyActivity> findExpiredActivities(@Param("currentDate") LocalDateTime currentDate);
}
