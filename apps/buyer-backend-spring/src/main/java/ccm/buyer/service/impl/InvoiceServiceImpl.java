package ccm.buyer.service.impl;

import ccm.buyer.entity.Invoice;
import ccm.buyer.entity.Transaction;
import ccm.buyer.repository.InvoiceRepository;
import ccm.buyer.repository.TransactionRepository;
import ccm.buyer.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repo;
    private final TransactionRepository trRepo;

    @Override
    public Invoice issueInvoice(Long trId) {
        Transaction tr = trRepo.findById(trId).orElseThrow();
        Invoice inv = Invoice.builder()
                .trId(trId)
                .filePath("/invoices/inv_" + trId + ".pdf")
                .issueDate(LocalDateTime.now())
                .build();
        return repo.save(inv);
    }

    @Override
    public List<Invoice> listInvoicesByBuyer(Long buyerId) {
        return repo.findAll();
    }
}
