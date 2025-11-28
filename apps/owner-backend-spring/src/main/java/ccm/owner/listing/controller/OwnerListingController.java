package ccm.owner.listing.controller;

import ccm.admin.credit.entity.CarbonCredit;
import ccm.admin.credit.entity.enums.CreditStatus;
import ccm.admin.user.entity.User;
import ccm.admin.user.repository.UserRepository;
import ccm.admin.journey.repository.JourneyRepository;
import ccm.admin.journey.entity.Journey;
// Import Enum Status của Journey để lọc hành trình đã duyệt
import ccm.admin.journey.entity.enums.JourneyStatus; 

import ccm.owner.listing.dto.CreateListingRequest;
import ccm.owner.listing.repository.OwnerCreditRepository;
import ccm.owner.listing.repository.OwnerListingRepository;
import ccm.owner.wallet.dto.response.WalletBalanceResponse;
import ccm.owner.wallet.service.OwnerWalletService;
import ccm.owner.listing.entity.Listing;
import ccm.owner.listing.entity.ListingType;
import ccm.owner.listing.entity.ListingStatus;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/owner/listings")
@RequiredArgsConstructor
public class OwnerListingController {

    private final OwnerListingRepository listingRepository;
    private final OwnerCreditRepository creditRepository;
    private final UserRepository userRepository;
    private final JourneyRepository journeyRepository;
    private final OwnerWalletService walletService;

    // --- [PHẦN 1: MỚI] API lấy danh sách bài đăng của tôi ---
    // (Frontend cần cái này để tính toán số lượng đang bị khóa/Locked)
    @GetMapping
    public ResponseEntity<List<Listing>> getMyListings() {
        User owner = getCurrentUser();
        List<Listing> myListings = listingRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("owner"), owner)
        );
        return ResponseEntity.ok(myListings);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createListing(@Valid @RequestBody CreateListingRequest request) {
        User owner = getCurrentUser();

        // --- 1. KIỂM TRA SỐ DƯ TỪ SERVICE ---
        // Sử dụng logic chuẩn từ WalletService
        WalletBalanceResponse balance = walletService.getMyBalance();
        BigDecimal availableBalance = balance.getAvailableCredits(); // Lấy trường mới thêm

       

        if (availableBalance.compareTo(request.getAmount()) < 0) {
            return ResponseEntity.badRequest().body(
                "Không đủ tín chỉ khả dụng! (Có: " + availableBalance + ", Muốn bán: " + request.getAmount() + ")"
            );
        }

        // --- 2. TÌM HÀNH TRÌNH ĐẠI DIỆN (Dùng làm bằng chứng nguồn gốc) ---
        // Lấy hành trình mới nhất đã verify
        List<Journey> verifiedJourneys = journeyRepository.findAll((root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            return cb.and(
                cb.equal(root.get("userId"), owner.getId()),
                cb.equal(root.get("status"), JourneyStatus.VERIFIED)
            );
        });

        if (verifiedJourneys.isEmpty()) {
             return ResponseEntity.badRequest().body("Bạn cần có ít nhất 1 hành trình đã được duyệt (Verified) để bán tín chỉ.");
        }
        
        Long representativeJourneyId = verifiedJourneys.get(0).getId();

        // --- 3. TẠO DỮ LIỆU (Tín chỉ & Listing) ---
        
        // Tạo CarbonCredit (Entity quản lý bởi Admin)
        CarbonCredit credit = CarbonCredit.builder()
                .ownerId(owner.getId())
                .journeyId(representativeJourneyId)
                .amount(request.getAmount())
                .status(CreditStatus.LISTED)
                .pricePerCredit(request.getPricePerCredit())
                .build();
        
        creditRepository.save(credit);

        // Tạo Listing (Entity hiển thị trên chợ)
        Listing listing = Listing.builder()
                .carbonCreditId(credit.getId())
                .owner(owner)
                .title("Tín chỉ Carbon từ " + owner.getFullName())
                .description("Tín chỉ đã được xác minh từ hành trình #" + representativeJourneyId)
                .quantity(request.getAmount())
                .price(request.getPricePerCredit())
                .unit("tCO2")
                .listingType(ListingType.FIXED_PRICE)
                .status(ListingStatus.PENDING) // Chờ duyệt
                .build();
        
        listingRepository.save(listing);

        return ResponseEntity.ok("Tạo bài đăng bán thành công! Đang chờ duyệt.");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}