package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.response.ReceiptResponse;

import java.util.List;

/**
 * Servicio para gestionar recibos de pagos.
 */
public interface IReceiptService {

    /**
     * Obtiene todos los recibos de un usuario.
     */
    List<ReceiptResponse> getUserReceipts(Long userId);

    /**
     * Obtiene un recibo específico por su número.
     */
    ReceiptResponse getReceiptByNumber(String receiptNumber);

    /**
     * Genera un recibo para una membresía.
     */
    ReceiptResponse generateReceipt(Long membershipId, String paymentIntentId);
}

