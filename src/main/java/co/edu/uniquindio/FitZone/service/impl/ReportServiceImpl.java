package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.response.BillingReportResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipReportResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.Receipt;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.ReceiptRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de generación de reportes.
 */
@Service
public class ReportServiceImpl implements IReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final MembershipRepository membershipRepository;
    private final ReceiptRepository receiptRepository;
    private final MembershipTypeRepository membershipTypeRepository;

    public ReportServiceImpl(MembershipRepository membershipRepository,
                            ReceiptRepository receiptRepository,
                            MembershipTypeRepository membershipTypeRepository) {
        this.membershipRepository = membershipRepository;
        this.receiptRepository = receiptRepository;
        this.membershipTypeRepository = membershipTypeRepository;
    }

    @Override
    public MembershipReportResponse generateMembershipReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generando reporte de membresías - Período: {} a {}", startDate, endDate);

        List<Membership> memberships = membershipRepository.findByStartDateBetween(startDate, endDate);
        List<MembershipReportResponse.MembershipSummary> summaries = memberships.stream()
                .map(this::mapToMembershipSummary)
                .collect(Collectors.toList());

        logger.info("Reporte de membresías generado - Total: {} membresías", summaries.size());
        return MembershipReportResponse.create(startDate, endDate, summaries);
    }

    @Override
    public BillingReportResponse generateBillingReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generando reporte de facturación - Período: {} a {}", startDate, endDate);

        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Receipt> receipts = receiptRepository.findByDateRange(startDateTime, endDateTime);
        List<BillingReportResponse.TransactionSummary> transactions = receipts.stream()
                .map(this::mapToTransactionSummary)
                .collect(Collectors.toList());

        logger.info("Reporte de facturación generado - Total: {} transacciones", transactions.size());
        return BillingReportResponse.create(startDate, endDate, transactions);
    }

    @Override
    public MembershipReportResponse generateMembershipReportByStatus(MembershipStatus status) {
        logger.info("Generando reporte de membresías por estado: {}", status);

        List<Membership> memberships = membershipRepository.findByStatus(status);
        List<MembershipReportResponse.MembershipSummary> summaries = memberships.stream()
                .map(this::mapToMembershipSummary)
                .collect(Collectors.toList());

        logger.info("Reporte por estado generado - Total: {} membresías con estado {}", summaries.size(), status);
        return MembershipReportResponse.create(null, null, summaries);
    }

    @Override
    public MembershipReportResponse generateMembershipReportByType(MembershipTypeName membershipType) {
        logger.info("Generando reporte de membresías por tipo: {}", membershipType);

        // Buscar el MembershipType por su nombre
        MembershipType type = membershipTypeRepository.findByName(membershipType)
                .orElseThrow(() -> new RuntimeException("Tipo de membresía no encontrado: " + membershipType));

        // Buscar membresías por membershipTypeId
        List<Membership> memberships = membershipRepository.findByMembershipTypeId(type.getIdMembershipType());
        List<MembershipReportResponse.MembershipSummary> summaries = memberships.stream()
                .map(this::mapToMembershipSummary)
                .collect(Collectors.toList());

        logger.info("Reporte por tipo generado - Total: {} membresías tipo {}", summaries.size(), membershipType);
        return MembershipReportResponse.create(null, null, summaries);
    }

    @Override
    public MembershipReportResponse generateExpiringMembershipsReport(int daysAhead) {
        logger.info("Generando reporte de membresías que vencen en {} días", daysAhead);

        LocalDate cutoffDate = LocalDate.now().plusDays(daysAhead);
        List<Membership> memberships = membershipRepository.findByStatusAndEndDateBefore(MembershipStatus.ACTIVE, cutoffDate);

        List<MembershipReportResponse.MembershipSummary> summaries = memberships.stream()
                .filter(m -> m.getEndDate().isAfter(LocalDate.now())) // Solo incluir las que aún no han expirado
                .map(this::mapToMembershipSummary)
                .collect(Collectors.toList());

        logger.info("Reporte de membresías próximas a vencer generado - Total: {} membresías", summaries.size());
        return MembershipReportResponse.create(LocalDate.now(), cutoffDate, summaries);
    }

    private MembershipReportResponse.MembershipSummary mapToMembershipSummary(Membership membership) {
        LocalDate today = LocalDate.now();
        int daysUntilExpiry = (int) ChronoUnit.DAYS.between(today, membership.getEndDate());
        boolean isExpiring = daysUntilExpiry <= 30 && daysUntilExpiry > 0;

        return new MembershipReportResponse.MembershipSummary(
                membership.getIdMembership(),
                membership.getUser().getIdUser(),
                membership.getUser().getPersonalInformation().getFirstName() + " " +
                        membership.getUser().getPersonalInformation().getLastName(),
                membership.getUser().getEmail(),
                membership.getUser().getPersonalInformation().getDocumentNumber(),
                membership.getType().getName(),
                membership.getStatus(),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getLocation().getName(),
                isExpiring,
                daysUntilExpiry
        );
    }

    private BillingReportResponse.TransactionSummary mapToTransactionSummary(Receipt receipt) {
        return new BillingReportResponse.TransactionSummary(
                receipt.getReceiptNumber(),
                receipt.getId(), // usando el ID del receipt como membershipId temporal
                receipt.getUserId(),
                receipt.getUserName(),
                receipt.getMembershipType(),
                receipt.getAmount(),
                receipt.getCurrency(),
                receipt.getPaymentMethod(),
                receipt.getGeneratedAt(),
                "MEMBERSHIP_PAYMENT" // tipo de recibo por defecto
        );
    }
}
