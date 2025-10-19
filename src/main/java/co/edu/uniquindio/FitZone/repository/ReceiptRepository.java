package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByUserIdOrderByGeneratedAtDesc(Long userId);
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    @Query("SELECT r FROM Receipt r WHERE r.generatedAt BETWEEN :startDate AND :endDate")
    List<Receipt> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
