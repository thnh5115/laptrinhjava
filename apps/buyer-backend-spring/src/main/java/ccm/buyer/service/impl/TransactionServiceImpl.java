package ccm.buyer.service.impl;

import ccm.buyer.dto.request.CreateTransactionRequest;
import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.entity.Listing;
import ccm.buyer.entity.Payment;
import ccm.buyer.entity.Transaction;
import ccm.buyer.enums.PayStatus;
import ccm.buyer.enums.TrStatus;
import ccm.buyer.exception.NotFoundException;
import ccm.buyer.repository.TransactionRepository;
import ccm.buyer.service.InvoiceService;
import ccm.buyer.service.ListingService;
import ccm.buyer.service.NotificationService;
import ccm.buyer.service.PaymentService;
import ccm.buyer.service.TransactionService;
       
    
import ccm.buyer.entity.CarbonCredit;

import ccm.buyer.entity.EWallet; // Import mới
import ccm.buyer.repository.CarbonCreditRepository;
import ccm.buyer.repository.EWalletRepository; // Import mới
import ccm.buyer.repository.ListingRepository; // Import mới
import ccm.buyer.enums.ListingStatus;



import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

private final TransactionRepository transactionRepository;
private final ListingService listingService;
private final PaymentService paymentService;
private final InvoiceService invoiceService;
private final NotificationService notifyService;
private final EWalletRepository eWalletRepository;
    private final ListingRepository listingRepository;
    private final CarbonCreditRepository creditRepository; // Cần inject thêm cái này
    private final EWalletRepository walletRepository;

  @Override
  public List<TransactionResponse> list(Long buyerId) {
    List<Transaction> data = (buyerId == null)
        ? transactionRepository.findAll()
        : transactionRepository.findByBuyerId(buyerId);
    return data.stream().map(this::map).toList();
  }

  @Override
@Transactional
public TransactionResponse create(CreateTransactionRequest req) {
    // 1. Validate Listing (Không cần check User nữa, tin tưởng ID từ Token gửi xuống)
    Listing listing = listingService.validateOpen(req.getListingId());
    
    // 2. Tính tiền
    BigDecimal price = (listing.getPrice() != null ? listing.getPrice() : BigDecimal.ZERO);
    BigDecimal total = price.multiply(req.getQty()).setScale(2, RoundingMode.HALF_UP);

    // 3. Lưu Transaction
    Transaction tx = Transaction.builder()
            .buyerId(req.getBuyerId())
            .listingId(req.getListingId())
            .qty(req.getQty())
            .amount(total)
            .status(TrStatus.PENDING)
            .type("CREDIT_PURCHASE")
            .build();

    tx = transactionRepository.save(tx);

    // 4. Xử lý thanh toán (Giữ nguyên logic cũ)
    try {
        Payment pay = paymentService.processPayment(tx.getId(), "WALLET", total);
        if (pay.getStatus() == PayStatus.SUCCESS) {
          listing.setStatus(ListingStatus.SOLD);
            BigDecimal newQty = listing.getQty().subtract(req.getQty());
                listing.setQty(newQty);
                
                // Nếu hết hàng -> Đổi trạng thái thành SOLD
                if (newQty.compareTo(BigDecimal.ZERO) == 0) {
                    listing.setStatus(ListingStatus.SOLD);
                }
                listingRepository.save(listing);

                // B. CỘNG TIỀN CHO NGƯỜI BÁN (Update Wallet)
                EWallet sellerWallet = eWalletRepository.findByUserId(listing.getSellerId())
                        .orElseThrow(() -> new RuntimeException("Ví người bán không tồn tại!"));
                
                sellerWallet.setBalance(sellerWallet.getBalance().add(total));
                sellerWallet.setUpdatedAt(LocalDateTime.now());
                eWalletRepository.save(sellerWallet);

                // C. Hoàn tất giao dịch
                tx.setStatus(TrStatus.COMPLETED);
                transactionRepository.save(tx);
                
                invoiceService.issueInvoice(tx.getId());
                notifyService.notifyBuyer(req.getBuyerId(), "Mua thành công " + req.getQty() + " tCO2!");
        } else {
            tx.setStatus(TrStatus.FAILED);
            transactionRepository.save(tx);
            notifyService.notifyBuyer(req.getBuyerId(), "Payment failed.");
        }
    } catch (Exception ex) {
        tx.setStatus(TrStatus.FAILED);
            transactionRepository.save(tx);
            notifyService.notifyBuyer(req.getBuyerId(), "ERROR: " + ex.getMessage());
            throw ex;
    }

    return map(tx);
}

  @Override
  public TransactionResponse updateStatus(Long id, TrStatus status) {
    Transaction tx = transactionRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    tx.setStatus(status);
    tx = transactionRepository.save(tx);
    return map(tx);
  }

  private TransactionResponse map(Transaction t) {
    return TransactionResponse.builder()
        .id(t.getId())
        .buyerId(t.getBuyerId())
        .listingId(t.getListingId())
        .qty(t.getQty())
        .amount(t.getAmount())
        .status(t.getStatus())
        .createdAt(t.getCreatedAt())
        .build();
  }
  public TransactionResponse buyListing(Long buyerId, Long listingId, BigDecimal qtyRequested) { 
        // 1. Lấy thông tin Listing (Dùng Entity của Buyer)
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        // Validate: Listing phải đang mở (APPROVED hoặc OPEN)
        if (listing.getStatus() != ListingStatus.APPROVED && listing.getStatus() != ListingStatus.OPEN) {
            throw new IllegalStateException("This listing is no longer available.");
        }

        // 2. [FIX LOGIC] MUA ĐỨT (BUY ALL)
        // Lấy toàn bộ số lượng còn lại của Listing
        BigDecimal amountToBuy = listing.getQty(); 
        BigDecimal totalCost = amountToBuy.multiply(listing.getPrice());

        // 3. Kiểm tra số dư Buyer (Ví dụ logic ví)
        EWallet buyerWallet = walletRepository.findByUserId(buyerId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (buyerWallet.getBalance().compareTo(totalCost) < 0) {
             throw new IllegalStateException("Insufficient funds.");
        }

        // 4. Trừ tiền Buyer (Logic ví tiền)
        buyerWallet.setBalance(buyerWallet.getBalance().subtract(totalCost));
        walletRepository.save(buyerWallet);
        
        // Cộng tiền cho Seller (Cần tìm ví seller)
        EWallet sellerWallet = walletRepository.findByUserId(listing.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller wallet not found"));
        sellerWallet.setBalance(sellerWallet.getBalance().add(totalCost));
        walletRepository.save(sellerWallet);

        // 5. [QUAN TRỌNG] Cập nhật trạng thái Listing thành ĐÃ BÁN (SOLD)
        listing.setStatus(ListingStatus.SOLD); // Enum của Buyer [cite: 2546]
        listing.setQty(BigDecimal.ZERO);       // Set về 0
        listingRepository.save(listing);

        // 6. Cập nhật trạng thái Carbon Credit gốc thành SOLD
        if (listing.getCarbonCreditId() != null) {
            CarbonCredit credit = creditRepository.findById(listing.getCarbonCreditId()).orElse(null);
            if (credit != null) {
                // Bên Buyer, status là String 
                credit.setStatus("SOLD"); 
                // credit.setBuyerId(buyerId); // Nếu Entity Buyer có trường này thì set, không thì bỏ qua
                // credit.setSoldAt(LocalDateTime.now()); // Tương tự
                creditRepository.save(credit);
            }
        }

        // 7. Tạo Transaction Log
        Transaction transaction = Transaction.builder()
                .buyerId(buyerId)
                // .sellerId(...) // Entity Transaction bên Buyer chưa có sellerId, nếu cần hãy thêm vào Entity
                .listingId(listing.getId())
                .qty(amountToBuy)
                .amount(totalCost) // Entity Transaction dùng 'amount' cho tổng tiền [cite: 2537]
                .status(TrStatus.COMPLETED)
                .type("CREDIT_PURCHASE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        // Return response (Map từ entity sang DTO)
        return TransactionResponse.builder()
                .id(transaction.getId())
                .buyerId(transaction.getBuyerId())
                .listingId(transaction.getListingId())
                .qty(transaction.getQty())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

