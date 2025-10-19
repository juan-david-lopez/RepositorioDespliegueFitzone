package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser_IdUser(Long userId);

    // Buscar reservas de un target (clase/espacio) que se solapen con un intervalo
    List<Reservation> findByTargetIdAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqual(
            Long targetId, LocalDateTime endDateTime, LocalDateTime startDateTime);

    // Buscar reservas que se solapen con un intervalo (cualquier target)
    List<Reservation> findByStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqual(
            LocalDateTime endDateTime, LocalDateTime startDateTime);

    // ✅ NUEVO: Buscar todas las clases grupales futuras y confirmadas
    @Query("SELECT r FROM Reservation r WHERE r.isGroup = true " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.startDateTime > :now " +
           "ORDER BY r.startDateTime ASC")
    List<Reservation> findUpcomingGroupClasses(@Param("now") LocalDateTime now);

    // ✅ NUEVO: Buscar clases grupales por rango de fechas
    @Query("SELECT r FROM Reservation r WHERE r.isGroup = true " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.startDateTime >= :startDate " +
           "AND r.startDateTime <= :endDate " +
           "ORDER BY r.startDateTime ASC")
    List<Reservation> findGroupClassesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ✅ NUEVO: Buscar reservas por tipo
    @Query("SELECT r FROM Reservation r WHERE r.reservationType = :type " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.startDateTime > :now " +
           "ORDER BY r.startDateTime ASC")
    List<Reservation> findByReservationTypeAndFuture(
            @Param("type") String type,
            @Param("now") LocalDateTime now);

    // ✅ FIX CRÍTICO: Buscar TODAS las reservas del usuario (creador O participante)
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "WHERE r.user.idUser = :userId " +
           "OR (:userId MEMBER OF r.participantUserIds)")
    List<Reservation> findAllByUserIdOrParticipant(@Param("userId") Long userId);

    // ✅ FIX CRÍTICO: Buscar reservas futuras del usuario (creador O participante)
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "WHERE (r.user.idUser = :userId OR :userId MEMBER OF r.participantUserIds) " +
           "AND r.startDateTime > :now " +
           "AND r.status = 'CONFIRMED' " +
           "ORDER BY r.startDateTime ASC")
    List<Reservation> findUpcomingByUserIdOrParticipant(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);
}
