package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.AutoRenewalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoRenewalSettingsRepository extends JpaRepository<AutoRenewalSettings, Long> {
    Optional<AutoRenewalSettings> findByUserId(Long userId);
}

