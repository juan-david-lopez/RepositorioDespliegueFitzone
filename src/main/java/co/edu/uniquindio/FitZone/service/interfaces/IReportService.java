package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.response.MembershipReportResponse;
import co.edu.uniquindio.FitZone.dto.response.BillingReportResponse;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.time.LocalDate;

/**
 * Servicio para la generación de reportes de membresías y facturación.
 */
public interface IReportService {

    /**
     * Genera un reporte completo de membresías en un rango de fechas.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Reporte de membresías
     */
    MembershipReportResponse generateMembershipReport(LocalDate startDate, LocalDate endDate);

    /**
     * Genera un reporte de facturación en un rango de fechas.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Reporte de facturación
     */
    BillingReportResponse generateBillingReport(LocalDate startDate, LocalDate endDate);

    /**
     * Genera un reporte de membresías por estado.
     *
     * @param status Estado de membresía a filtrar
     * @return Reporte de membresías por estado
     */
    MembershipReportResponse generateMembershipReportByStatus(MembershipStatus status);

    /**
     * Genera un reporte de membresías por tipo.
     *
     * @param membershipType Tipo de membresía a filtrar
     * @return Reporte de membresías por tipo
     */
    MembershipReportResponse generateMembershipReportByType(MembershipTypeName membershipType);

    /**
     * Genera un reporte de membresías que vencen en los próximos días.
     *
     * @param daysAhead Número de días hacia adelante
     * @return Reporte de membresías próximas a vencer
     */
    MembershipReportResponse generateExpiringMembershipsReport(int daysAhead);
}
