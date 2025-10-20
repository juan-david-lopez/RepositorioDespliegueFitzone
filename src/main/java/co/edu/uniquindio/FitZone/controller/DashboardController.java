package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.response.DashboardResponse;
import co.edu.uniquindio.FitZone.service.interfaces.IDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/reports")
@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final IDashboardService dashboardService;

    public DashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<DashboardResponse> getDashboardStats() {
        logger.info("GET /api/v1/admin/reports/kpis - Consultando estadísticas del dashboard");
        try {
            DashboardResponse response = dashboardService.getDashboardStats();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al generar estadísticas del dashboard: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
