package ccm.buyer.service;

import ccm.buyer.entity.Invoice;
import java.util.List;

public interface InvoiceService {
    Invoice issueInvoice(Long trId);
    List<Invoice> listInvoicesByBuyer(Long buyerId);
}
