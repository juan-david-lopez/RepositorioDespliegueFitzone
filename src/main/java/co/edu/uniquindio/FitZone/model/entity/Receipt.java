package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String receiptNumber;

    @Column(nullable = false)
    private Long userId;

    private String userName;
    private String userEmail;
    private String userDocument;

    @Enumerated(EnumType.STRING)
    private MembershipTypeName membershipType;

    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false)
    private String currency = "COP";

    private String paymentMethod;
    private String paymentIntentId;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    private BigDecimal subtotal;
    private BigDecimal totalDiscounts;
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        if (receiptNumber == null) {
            receiptNumber = "REC-" + System.currentTimeMillis();
        }
    }
}
