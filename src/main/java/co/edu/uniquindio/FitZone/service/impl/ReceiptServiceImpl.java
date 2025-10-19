package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.response.ReceiptResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.Receipt;
import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.repository.ReceiptRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements IReceiptService {

    private final ReceiptRepository receiptRepository;
    private final MembershipRepository membershipRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getUserReceipts(Long userId) {
        List<Receipt> receipts = receiptRepository.findByUserIdOrderByGeneratedAtDesc(userId);
        return receipts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByNumber(String receiptNumber) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new RuntimeException("Recibo no encontrado"));
        return mapToResponse(receipt);
    }

    @Override
    @Transactional
    public ReceiptResponse generateReceipt(Long membershipId, String paymentIntentId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        User user = membership.getUser();

        Receipt receipt = Receipt.builder()
                .receiptNumber("REC-" + System.currentTimeMillis())
                .userId(user.getIdUser())
                .userName(user.getPersonalInformation().getFirstName() + " " + user.getPersonalInformation().getLastName())
                .userEmail(user.getEmail())
                .userDocument(user.getPersonalInformation().getDocumentNumber())
                .membershipType(membership.getType().getName())
                .membershipStartDate(membership.getStartDate())
                .membershipEndDate(membership.getEndDate())
                .amount(membership.getPrice())
                .currency("COP")
                .paymentIntentId(paymentIntentId)
                .generatedAt(LocalDateTime.now())
                .build();

        receipt = receiptRepository.save(receipt);
        return mapToResponse(receipt);
    }

    private ReceiptResponse mapToResponse(Receipt receipt) {
        return new ReceiptResponse(
                receipt.getId().toString(),
                receipt.getReceiptNumber(),
                receipt.getGeneratedAt(),
                null, // membershipId - no disponible en la entidad actual
                receipt.getUserId() != null ? receipt.getUserId().toString() : null,
                receipt.getUserName(),
                receipt.getUserEmail(),
                receipt.getUserDocument(),
                receipt.getMembershipType(),
                receipt.getMembershipStartDate(),
                receipt.getMembershipEndDate(),
                receipt.getAmount(),
                receipt.getCurrency(),
                receipt.getPaymentMethod(),
                receipt.getPaymentIntentId(),
                java.util.List.of(), // items - lista vacía por ahora
                receipt.getAmount(), // subtotal
                java.math.BigDecimal.ZERO, // totalDiscounts
                receipt.getAmount(), // total
                "FitZone", // gymName
                "Dirección del gimnasio", // gymAddress
                "Teléfono del gimnasio", // gymPhone
                "correo@fitzone.com", // gymEmail
                "123456789" // gymTaxId
        );
    }
}
