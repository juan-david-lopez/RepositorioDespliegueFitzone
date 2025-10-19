package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta con los datos del recibo electrónico generado.
 */
public record ReceiptResponse(
        String receiptId,
        String receiptNumber,
        LocalDateTime generatedAt,
        String membershipId,
        String userId,
        String userName,
        String userEmail,
        String userDocument,
        MembershipTypeName membershipType,
        LocalDate membershipStartDate,
        LocalDate membershipEndDate,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String paymentIntentId,
        List<ReceiptItem> items,
        BigDecimal subtotal,
        BigDecimal totalDiscounts,
        BigDecimal total,
        String gymName,
        String gymAddress,
        String gymPhone,
        String gymEmail,
        String gymTaxId
) {

    public record ReceiptItem(
            String description,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal total
    ) {}

    public static ReceiptResponse createMembershipReceipt(
            String receiptId,
            String receiptNumber,
            String membershipId,
            String userId,
            String userName,
            String userEmail,
            String userDocument,
            MembershipTypeName membershipType,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal amount,
            String paymentIntentId,
            List<ReceiptItem> items,
            BigDecimal subtotal,
            BigDecimal totalDiscounts
    ) {
        return new ReceiptResponse(
                receiptId,
                receiptNumber,
                LocalDateTime.now(),
                membershipId,
                userId,
                userName,
                userEmail,
                userDocument,
                membershipType,
                startDate,
                endDate,
                amount,
                "COP",
                "Tarjeta de Crédito/Débito",
                paymentIntentId,
                items,
                subtotal,
                totalDiscounts,
                amount,
                "FitZone",
                "Universidad del Quindío, Armenia, Colombia",
                "***-***-****",
                "fitzoneuq@gmail.com",
                "890.000.000-1"
        );
    }
}
