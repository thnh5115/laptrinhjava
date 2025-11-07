package ccm.admin.credit.repository;

import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.credit.entity.enums.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CarbonCreditRepository extends JpaRepository<CarbonCredit, Long>, JpaSpecificationExecutor<CarbonCredit> {

    /** Count credits by status */
    long countByStatus(CreditStatus status);

    /** Calculate total credits amount by status */
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CarbonCredit c WHERE c.status = :status")
    BigDecimal calculateTotalAmountByStatus(CreditStatus status);

    /** Calculate total revenue from sold credits */
    @Query("SELECT COALESCE(SUM(c.amount * c.pricePerCredit), 0) FROM CarbonCredit c WHERE c.status = 'SOLD'")
    BigDecimal calculateTotalRevenue();

    /** Get total count */
    @Query("SELECT COUNT(c) FROM CarbonCredit c")
    long getTotalCount();
}
