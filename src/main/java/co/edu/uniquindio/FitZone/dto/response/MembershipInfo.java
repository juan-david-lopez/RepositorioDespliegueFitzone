package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipInfo {
    private Long id;
    private MembershipType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String statusMessage;
    private long daysRemaining;
    private boolean isActive;
}
