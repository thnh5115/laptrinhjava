package ccm.buyer.repository;

import ccm.buyer.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;



public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
}
