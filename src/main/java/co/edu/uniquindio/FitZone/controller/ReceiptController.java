package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.ReceiptRequest;
import co.edu.uniquindio.FitZone.dto.response.ReceiptResponse;
import co.edu.uniquindio.FitZone.service.interfaces.IReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/receipts")
@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
public class ReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);
    private final IReceiptService receiptService;

    public ReceiptController(IReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<ReceiptResponse> createReceipt(@RequestBody ReceiptRequest receiptRequest) {
        logger.info("POST /api/v1/receipts - Creando recibo");
        try {
            ReceiptResponse receipt = receiptService.generateReceipt(receiptRequest.getMembershipId(), receiptRequest.getPaymentIntentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
        } catch (Exception e) {
            logger.error("Error al crear recibo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{receiptNumber}")
    public ResponseEntity<ReceiptResponse> getReceiptByNumber(@PathVariable String receiptNumber) {
        logger.debug("GET /api/v1/receipts/{} - Consultando recibo", receiptNumber);
        try {
            ReceiptResponse receipt = receiptService.getReceiptByNumber(receiptNumber);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            logger.error("Error al consultar recibo {}: {}", receiptNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByUser(@PathVariable Long userId) {
        logger.debug("GET /api/v1/receipts/user/{} - Consultando recibos de usuario", userId);
        try {
            List<ReceiptResponse> receipts = receiptService.getUserReceipts(userId);
            return ResponseEntity.ok(receipts);
        } catch (Exception e) {
            logger.error("Error al consultar recibos del usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
