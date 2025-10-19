package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.AutoRenewalSettingsRequest;
import co.edu.uniquindio.FitZone.dto.response.AutoRenewalResponse;

public interface IAutoRenewalService {
    AutoRenewalResponse getAutoRenewalSettings(Long userId);
    AutoRenewalResponse updateAutoRenewalSettings(Long userId, AutoRenewalSettingsRequest request);
    void processAutoRenewals();
    void checkExpiringMemberships();
}
