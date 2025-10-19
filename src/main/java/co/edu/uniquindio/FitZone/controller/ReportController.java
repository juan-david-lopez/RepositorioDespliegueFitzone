package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.response.BillingReportResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipReportResponse;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.service.interfaces.IReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador REST para la generación de reportes de membresías y facturación.
 */
@RestController
@RequestMapping("/reports")
@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final IReportService reportService;

    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/memberships")
    public ResponseEntity<MembershipReportResponse> generateMembershipReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("GET /reports/memberships - Generando reporte de membresías para período: {} a {}", startDate, endDate);

        try {
            MembershipReportResponse report = reportService.generateMembershipReport(startDate, endDate);
            logger.info("Reporte de membresías generado exitosamente - Total: {} membresías", report.totalMemberships());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error al generar reporte de membresías - Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/billing")
    public ResponseEntity<BillingReportResponse> generateBillingReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("GET /reports/billing - Generando reporte de facturación para período: {} a {}", startDate, endDate);

        try {
            BillingReportResponse report = reportService.generateBillingReport(startDate, endDate);
            logger.info("Reporte de facturación generado exitosamente - Total: {} transacciones, Ingresos: {}",
                    report.totalTransactions(), report.totalRevenue());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error al generar reporte de facturación - Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/memberships/status/{status}")
    public ResponseEntity<MembershipReportResponse> generateMembershipReportByStatus(@PathVariable MembershipStatus status) {
        logger.info("GET /reports/memberships/status/{} - Generando reporte por estado", status);

        try {
            MembershipReportResponse report = reportService.generateMembershipReportByStatus(status);
            logger.info("Reporte por estado generado exitosamente - Total: {} membresías con estado {}",
                    report.totalMemberships(), status);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error al generar reporte por estado - Estado: {}, Error: {}", status, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/memberships/type/{membershipType}")
    public ResponseEntity<MembershipReportResponse> generateMembershipReportByType(@PathVariable MembershipTypeName membershipType) {
        logger.info("GET /reports/memberships/type/{} - Generando reporte por tipo", membershipType);

        try {
            MembershipReportResponse report = reportService.generateMembershipReportByType(membershipType);
            logger.info("Reporte por tipo generado exitosamente - Total: {} membresías tipo {}",
                    report.totalMemberships(), membershipType);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error al generar reporte por tipo - Tipo: {}, Error: {}", membershipType, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/memberships/expiring")
    public ResponseEntity<MembershipReportResponse> generateExpiringMembershipsReport(
            @RequestParam(defaultValue = "30") int daysAhead) {

        logger.info("GET /reports/memberships/expiring - Generando reporte de membresías que vencen en {} días", daysAhead);

        try {
            MembershipReportResponse report = reportService.generateExpiringMembershipsReport(daysAhead);
            logger.info("Reporte de membresías próximas a vencer generado - Total: {} membresías", report.totalMemberships());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error al generar reporte de membresías próximas a vencer - Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
