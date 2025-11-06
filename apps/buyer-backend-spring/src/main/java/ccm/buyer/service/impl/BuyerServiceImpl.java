package ccm.buyer.service.impl;

import ccm.buyer.entity.Buyer;
import ccm.buyer.entity.Invoice;
import ccm.buyer.entity.Listing;
import ccm.buyer.entity.Payment;
import ccm.buyer.entity.Transaction;
import ccm.buyer.enums.PayStatus;
import ccm.buyer.enums.TrStatus;
import ccm.buyer.repository.BuyerRepository;
import ccm.buyer.repository.TransactionRepository;
import ccm.buyer.service.BuyerService;
import ccm.buyer.service.InvoiceService;
import ccm.buyer.service.ListingService;
import ccm.buyer.service.NotificationService;
import ccm.buyer.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepo;
    private final ListingService listingService;
    private final TransactionRepository trRepo;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final NotificationService notifyService;

    @Override
    @Transactional
    public Transaction directBuy(Long buyerId, Long listingId, Integer qty) {
        Buyer buyer = buyerRepo.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        Listing listing = listingService.validateOpen(listingId);
        listingService.reserve(listingId, qty);

        Double total = (listing.getPrice() != null ? listing.getPrice() : 0.0) * qty;

        Transaction tx = Transaction.builder()
                .buyerId(buyerId)
                .listingId(listingId)
                .qty(qty)
                .amount(total)
                .status(TrStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        tx = trRepo.save(tx);

        try {
            Payment pay = paymentService.processPayment(tx.getId(), "WALLET", total);

            if (pay.getStatus() == PayStatus.SUCCESS) {
                tx.setStatus(TrStatus.COMPLETED);
                trRepo.save(tx);

                Invoice inv = invoiceService.issueInvoice(tx.getId());
                notifyService.notifyBuyer(
                        buyerId,
                        "Purchase successful! Invoice #" + inv.getId()
                );
            } else {
                tx.setStatus(TrStatus.FAILED);
                trRepo.save(tx);
                listingService.release(listingId, qty);
                notifyService.notifyBuyer(buyerId, "Payment failed.");
            }
        } catch (Exception ex) {
            tx.setStatus(TrStatus.FAILED);
            trRepo.save(tx);
            listingService.release(listingId, qty);
            notifyService.notifyBuyer(buyerId, "Error: " + ex.getMessage());
        }

        return tx;
    }
}
