package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Respuesta con el reporte de membresías.
 */
public record MembershipReportResponse(
        LocalDateTime reportGeneratedAt,
        LocalDate startDate,
        LocalDate endDate,
        int totalMemberships,
        Map<MembershipStatus, Integer> membershipsByStatus,
        Map<MembershipTypeName, Integer> membershipsByType,
        List<MembershipSummary> memberships,
        ReportStatistics statistics
) {

    public record MembershipSummary(
            Long membershipId,
            Long userId,
            String userName,
            String userEmail,
            String userDocument,
            MembershipTypeName membershipType,
            MembershipStatus status,
            LocalDate startDate,
            LocalDate endDate,
            String locationName,
            boolean isExpiring,
            int daysUntilExpiry
    ) {}

    public record ReportStatistics(
            int activeMemberships,
            int expiredMemberships,
            int suspendedMemberships,
            int cancelledMemberships,
            int basicMemberships,
            int premiumMemberships,
            int eliteMemberships,
            int membershipsExpiringIn7Days,
            int membershipsExpiringIn30Days,
            double averageMembershipDuration
    ) {}

    public static MembershipReportResponse create(
            LocalDate startDate,
            LocalDate endDate,
            List<MembershipSummary> memberships
    ) {
        Map<MembershipStatus, Integer> byStatus = Map.of(
                MembershipStatus.ACTIVE, (int) memberships.stream().filter(m -> m.status() == MembershipStatus.ACTIVE).count(),
                MembershipStatus.EXPIRED, (int) memberships.stream().filter(m -> m.status() == MembershipStatus.EXPIRED).count(),
                MembershipStatus.SUSPENDED, (int) memberships.stream().filter(m -> m.status() == MembershipStatus.SUSPENDED).count(),
                MembershipStatus.CANCELLED, (int) memberships.stream().filter(m -> m.status() == MembershipStatus.CANCELLED).count()
        );

        Map<MembershipTypeName, Integer> byType = Map.of(
                MembershipTypeName.BASIC, (int) memberships.stream().filter(m -> m.membershipType() == MembershipTypeName.BASIC).count(),
                MembershipTypeName.PREMIUM, (int) memberships.stream().filter(m -> m.membershipType() == MembershipTypeName.PREMIUM).count(),
                MembershipTypeName.ELITE, (int) memberships.stream().filter(m -> m.membershipType() == MembershipTypeName.ELITE).count()
        );

        ReportStatistics stats = new ReportStatistics(
                byStatus.get(MembershipStatus.ACTIVE),
                byStatus.get(MembershipStatus.EXPIRED),
                byStatus.get(MembershipStatus.SUSPENDED),
                byStatus.get(MembershipStatus.CANCELLED),
                byType.get(MembershipTypeName.BASIC),
                byType.get(MembershipTypeName.PREMIUM),
                byType.get(MembershipTypeName.ELITE),
                (int) memberships.stream().filter(m -> m.daysUntilExpiry() <= 7 && m.daysUntilExpiry() > 0).count(),
                (int) memberships.stream().filter(m -> m.daysUntilExpiry() <= 30 && m.daysUntilExpiry() > 0).count(),
                0.0 // Se puede calcular la duración promedio si se necesita
        );

        return new MembershipReportResponse(
                LocalDateTime.now(),
                startDate,
                endDate,
                memberships.size(),
                byStatus,
                byType,
                memberships,
                stats
        );
    }
}
