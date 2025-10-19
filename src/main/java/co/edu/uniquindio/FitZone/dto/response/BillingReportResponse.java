package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Respuesta con el reporte de facturación.
 */
public record BillingReportResponse(
        LocalDateTime reportGeneratedAt,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalRevenue,
        int totalTransactions,
        Map<MembershipTypeName, BigDecimal> revenueByMembershipType,
        Map<String, BigDecimal> revenueByPaymentMethod,
        List<TransactionSummary> transactions,
        BillingStatistics statistics
) {

    public record TransactionSummary(
            String receiptNumber,
            Long membershipId,
            Long userId,
            String userName,
            MembershipTypeName membershipType,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            LocalDateTime transactionDate,
            String transactionType
    ) {}

    public record BillingStatistics(
            BigDecimal averageTransactionAmount,
            BigDecimal totalNewMemberships,
            BigDecimal totalRenewals,
            int newMembershipsCount,
            int renewalsCount,
            BigDecimal basicMembershipRevenue,
            BigDecimal premiumMembershipRevenue,
            BigDecimal eliteMembershipRevenue,
            BigDecimal dailyAverage,
            BigDecimal monthlyProjection
    ) {}

    public static BillingReportResponse create(
            LocalDate startDate,
            LocalDate endDate,
            List<TransactionSummary> transactions
    ) {
        BigDecimal totalRevenue = transactions.stream()
                .map(TransactionSummary::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<MembershipTypeName, BigDecimal> revenueByType = Map.of(
                MembershipTypeName.BASIC, transactions.stream()
                        .filter(t -> t.membershipType() == MembershipTypeName.BASIC)
                        .map(TransactionSummary::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                MembershipTypeName.PREMIUM, transactions.stream()
                        .filter(t -> t.membershipType() == MembershipTypeName.PREMIUM)
                        .map(TransactionSummary::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                MembershipTypeName.ELITE, transactions.stream()
                        .filter(t -> t.membershipType() == MembershipTypeName.ELITE)
                        .map(TransactionSummary::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        Map<String, BigDecimal> revenueByPayment = Map.of(
                "Tarjeta de Crédito/Débito", transactions.stream()
                        .filter(t -> t.paymentMethod().contains("Tarjeta"))
                        .map(TransactionSummary::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        BigDecimal averageAmount = transactions.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(transactions.size()), 2, BigDecimal.ROUND_HALF_UP);

        int newMemberships = (int) transactions.stream()
                .filter(t -> t.transactionType().equals("MEMBERSHIP"))
                .count();

        int renewals = (int) transactions.stream()
                .filter(t -> t.transactionType().equals("RENEWAL"))
                .count();

        BigDecimal newMembershipRevenue = transactions.stream()
                .filter(t -> t.transactionType().equals("MEMBERSHIP"))
                .map(TransactionSummary::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal renewalRevenue = transactions.stream()
                .filter(t -> t.transactionType().equals("RENEWAL"))
                .map(TransactionSummary::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BillingStatistics stats = new BillingStatistics(
                averageAmount,
                newMembershipRevenue,
                renewalRevenue,
                newMemberships,
                renewals,
                revenueByType.get(MembershipTypeName.BASIC),
                revenueByType.get(MembershipTypeName.PREMIUM),
                revenueByType.get(MembershipTypeName.ELITE),
                totalRevenue.divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_HALF_UP), // Promedio diario aproximado
                totalRevenue // Proyección mensual básica
        );

        return new BillingReportResponse(
                LocalDateTime.now(),
                startDate,
                endDate,
                totalRevenue,
                transactions.size(),
                revenueByType,
                revenueByPayment,
                transactions,
                stats
        );
    }
}
