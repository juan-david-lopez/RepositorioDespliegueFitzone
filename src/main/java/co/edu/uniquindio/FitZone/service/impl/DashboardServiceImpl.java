package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.response.DashboardResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.Receipt;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.repository.ReceiptRepository;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final ReceiptRepository receiptRepository;

    public DashboardServiceImpl(MembershipRepository membershipRepository,
                                UserRepository userRepository,
                                ReceiptRepository receiptRepository) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.receiptRepository = receiptRepository;
    }

    @Override
    public DashboardResponse getDashboardStats() {
        logger.info("Generando estadísticas del dashboard administrativo");

        DashboardResponse.KPIData kpis = calculateKPIs();
        List<DashboardResponse.RevenueData> revenueHistory = calculateRevenueHistory();
        Map<String, Integer> membershipDistribution = calculateMembershipDistribution();
        Map<String, Integer> paymentMethodDistribution = calculatePaymentMethodDistribution();
        List<DashboardResponse.ExpiringMembership> expiringMemberships = getExpiringMemberships();

        DashboardResponse response = DashboardResponse.builder()
                .kpis(kpis)
                .revenueHistory(revenueHistory)
                .membershipDistribution(membershipDistribution)
                .paymentMethodDistribution(paymentMethodDistribution)
                .expiringMemberships(expiringMemberships)
                .build();

        logger.info("Estadísticas del dashboard generadas exitosamente");
        return response;
    }

    private DashboardResponse.KPIData calculateKPIs() {
        logger.debug("Calculando KPIs");

        List<Membership> activeMemberships = membershipRepository.findByStatus(MembershipStatus.ACTIVE);
        int totalActiveMembers = activeMemberships.size();

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Membership> newMembershipsThisMonth = membershipRepository.findByStartDateBetween(firstDayOfMonth, lastDayOfMonth);
        int newMembersThisMonth = newMembershipsThisMonth.size();

        List<Receipt> monthlyReceipts = receiptRepository.findAll().stream()
                .filter(r -> r.getGeneratedAt().toLocalDate().isAfter(firstDayOfMonth.minusDays(1)))
                .collect(Collectors.toList());

        BigDecimal monthlyRevenue = monthlyReceipts.stream()
                .map(Receipt::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRevenuePerMember = totalActiveMembers > 0
                ? monthlyRevenue.divide(BigDecimal.valueOf(totalActiveMembers), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<Membership> expiringThisMonth = membershipRepository.findByEndDateBetween(LocalDate.now(), thirtyDaysFromNow);
        int expiringCount = expiringThisMonth.size();

        double renewalRate = calculateRenewalRate();

        List<Membership> cancelledMemberships = membershipRepository.findByStatus(MembershipStatus.CANCELLED);
        int cancelledCount = cancelledMemberships.size();

        return DashboardResponse.KPIData.builder()
                .totalActiveMembers(totalActiveMembers)
                .newMembersThisMonth(newMembersThisMonth)
                .monthlyRevenue(monthlyRevenue)
                .averageRevenuePerMember(averageRevenuePerMember)
                .expiringThisMonth(expiringCount)
                .renewalRate(renewalRate)
                .totalMemberships((int) membershipRepository.count())
                .cancelledMemberships(cancelledCount)
                .build();
    }

    private List<DashboardResponse.RevenueData> calculateRevenueHistory() {
        logger.debug("Calculando historial de ingresos");

        List<DashboardResponse.RevenueData> revenueHistory = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = currentDate.minusMonths(i);
            LocalDate firstDay = monthDate.withDayOfMonth(1);
            LocalDate lastDay = monthDate.withDayOfMonth(monthDate.lengthOfMonth());

            List<Receipt> monthReceipts = receiptRepository.findAll().stream()
                    .filter(r -> {
                        LocalDate receiptDate = r.getGeneratedAt().toLocalDate();
                        return !receiptDate.isBefore(firstDay) && !receiptDate.isAfter(lastDay);
                    })
                    .collect(Collectors.toList());

            BigDecimal monthRevenue = monthReceipts.stream()
                    .map(Receipt::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int membershipsCount = membershipRepository.findByStartDateBetween(firstDay, lastDay).size();

            revenueHistory.add(DashboardResponse.RevenueData.builder()
                    .month(monthDate.format(DateTimeFormatter.ofPattern("MMM yyyy")))
                    .revenue(monthRevenue)
                    .memberships(membershipsCount)
                    .build());
        }

        return revenueHistory;
    }

    private Map<String, Integer> calculateMembershipDistribution() {
        logger.debug("Calculando distribución de membresías");

        List<Membership> activeMemberships = membershipRepository.findByStatus(MembershipStatus.ACTIVE);

        Map<String, Integer> distribution = activeMemberships.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getType().getName().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return distribution;
    }

    private Map<String, Integer> calculatePaymentMethodDistribution() {
        logger.debug("Calculando distribución de métodos de pago");

        List<Receipt> receipts = receiptRepository.findAll();

        Map<String, Integer> distribution = receipts.stream()
                .filter(r -> r.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(
                        Receipt::getPaymentMethod,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return distribution;
    }

    private List<DashboardResponse.ExpiringMembership> getExpiringMemberships() {
        logger.debug("Obteniendo membresías próximas a vencer");

        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<Membership> expiringMemberships = membershipRepository.findByEndDateBetween(LocalDate.now(), thirtyDaysFromNow);

        return expiringMemberships.stream()
                .map(m -> {
                    long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), m.getEndDate());
                    return DashboardResponse.ExpiringMembership.builder()
                            .userId(m.getUser().getIdUser())
                            .userName(m.getUser().getPersonalInformation().getFirstName() + " " +
                                    m.getUser().getPersonalInformation().getLastName())
                            .email(m.getUser().getEmail())
                            .membershipType(m.getType().getName().name())
                            .expirationDate(m.getEndDate().toString())
                            .daysRemaining((int) daysRemaining)
                            .build();
                })
                .sorted(Comparator.comparingInt(DashboardResponse.ExpiringMembership::getDaysRemaining))
                .limit(10)
                .collect(Collectors.toList());
    }

    private double calculateRenewalRate() {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        List<Membership> expiredMemberships = membershipRepository.findByEndDateBetween(threeMonthsAgo, LocalDate.now());

        if (expiredMemberships.isEmpty()) {
            return 0.0;
        }

        long renewedCount = expiredMemberships.stream()
                .filter(m -> {
                    List<Membership> userMemberships = membershipRepository.findByUserIdOrderByStartDateDesc(m.getUserId());
                    return userMemberships.size() > 1;
                })
                .count();

        return (double) renewedCount / expiredMemberships.size() * 100;
    }
}
