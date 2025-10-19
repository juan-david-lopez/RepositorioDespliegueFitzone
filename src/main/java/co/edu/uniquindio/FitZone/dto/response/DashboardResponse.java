package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private KPIData kpis;
    private List<RevenueData> revenueHistory;
    private Map<String, Integer> membershipDistribution;
    private Map<String, Integer> paymentMethodDistribution;
    private List<ExpiringMembership> expiringMemberships;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIData {
        private int totalActiveMembers;
        private int newMembersThisMonth;
        private BigDecimal monthlyRevenue;
        private BigDecimal averageRevenuePerMember;
        private int expiringThisMonth;
        private double renewalRate;
        private int totalMemberships;
        private int cancelledMemberships;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueData {
        private String month;
        private BigDecimal revenue;
        private int memberships;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpiringMembership {
        private Long userId;
        private String userName;
        private String email;
        private String membershipType;
        private String expirationDate;
        private int daysRemaining;
    }
}
