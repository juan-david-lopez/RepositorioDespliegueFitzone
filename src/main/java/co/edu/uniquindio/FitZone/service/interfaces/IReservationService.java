package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.CreateReservationRequest;
import co.edu.uniquindio.FitZone.dto.response.ReservationResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface IReservationService {

    List<ReservationResponse> listReservationsByUserId(Long userId) throws Exception;

    List<ReservationResponse> getUpcomingReservations(Long userId) throws Exception;

    ReservationResponse createReservation(Long userId, CreateReservationRequest request) throws Exception;

    boolean checkAvailability(Long targetId, LocalDateTime start, LocalDateTime end) throws Exception;

    ReservationResponse cancelReservation(Long userId, Long reservationId) throws Exception;

    // ✅ NUEVO: Obtener todas las clases grupales disponibles (visible para todos los miembros)
    List<ReservationResponse> getAvailableGroupClasses() throws Exception;

    // ✅ NUEVO: Unirse a una clase grupal existente (solo ELITE gratis)
    ReservationResponse joinGroupClass(Long userId, Long groupClassId) throws Exception;

    // ✅ NUEVO: Unirse a una clase grupal con pago (PREMIUM/BASIC)
    ReservationResponse joinGroupClassWithPayment(Long userId, Long groupClassId, String paymentMethodId) throws Exception;
}
