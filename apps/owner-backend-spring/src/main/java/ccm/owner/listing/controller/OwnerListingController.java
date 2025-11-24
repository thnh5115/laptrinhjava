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

        // --- [PHẦN 2: MỚI] LOGIC TÍNH TOÁN SỐ DƯ KHẢ DỤNG ---
        
        // A. Tính tổng tín chỉ kiếm được (từ các Journey đã được duyệt)
        List<Journey> verifiedJourneys = journeyRepository.findAll((root, query, cb) -> 
            cb.and(
                cb.equal(root.get("userId"), owner.getId()),
                cb.or(
                    cb.equal(root.get("status"), JourneyStatus.VERIFIED), // Hoặc VERIFIED tùy DB bạn
                    cb.equal(root.get("status"), JourneyStatus.VERIFIED)
                )
            )
        );

        // Cộng dồn số tín chỉ (giả sử Journey có field creditsGenerated, nếu null thì tính là 0)
        BigDecimal totalEarned = verifiedJourneys.stream()
                .map(j -> j.getCreditsGenerated() != null ? j.getCreditsGenerated() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // B. Tính tổng tín chỉ đang bị "khóa" (Đang rao bán hoặc chờ duyệt)
        List<Listing> activeListings = listingRepository.findAll((root, query, cb) -> 
            cb.and(
                cb.equal(root.get("owner"), owner),
                root.get("status").in(ListingStatus.PENDING, ListingStatus.APPROVED, ListingStatus.OPEN)
            )
        );

        BigDecimal totalLocked = activeListings.stream()
                .map(Listing::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // C. Kiểm tra: Số dư khả dụng có đủ để bán không?
        BigDecimal availableBalance = totalEarned.subtract(totalLocked);

        if (availableBalance.compareTo(request.getAmount()) < 0) {
            return ResponseEntity.badRequest().body(
                "Không đủ tín chỉ khả dụng! (Có: " + totalEarned + ", Đang bán: " + totalLocked + 
                ", Còn lại: " + availableBalance + ")"
            );
        }

        // --- [PHẦN 3: CODE CŨ CỦA BẠN] TẠO LISTING VỚI HÀNH TRÌNH ĐẠI DIỆN ---
        
        // Lấy hành trình mới nhất làm đại diện (để tránh lỗi khóa ngoại 409)
        // (Sắp xếp giảm dần theo ID)
        Long representativeJourneyId;
        if (!verifiedJourneys.isEmpty()) {
             // Lấy từ list verified đã query ở trên (phần tử cuối cùng thường là mới nhất hoặc sort lại)
             representativeJourneyId = verifiedJourneys.get(verifiedJourneys.size() - 1).getId();
        } else {
             // Fallback: Nếu user chưa có hành trình nào đã duyệt, nhưng bằng cách nào đó lại lọt xuống đây
             // (Thực ra đoạn check balance ở trên đã chặn rồi nếu totalEarned = 0)
             // Tìm tất cả hành trình kể cả chưa duyệt để lấy ID
             List<Journey> allJourneys = journeyRepository.findAll((root, query, cb) -> {
                query.orderBy(cb.desc(root.get("id")));
                return cb.equal(root.get("userId"), owner.getId());
             });
             
             if (allJourneys.isEmpty()) {
                 return ResponseEntity.badRequest().body("Bạn cần upload ít nhất 1 hành trình trước.");
             }
             representativeJourneyId = allJourneys.get(0).getId();
        }

        // Tạo Carbon Credit
        CarbonCredit credit = CarbonCredit.builder()
                .ownerId(owner.getId())
                .journeyId(representativeJourneyId)
                .amount(request.getAmount())
                .status(CreditStatus.LISTED)
                .pricePerCredit(request.getPricePerCredit())
                .build();
        
        creditRepository.save(credit);

        // Tạo Listing
        Listing listing = Listing.builder()
                .carbonCreditId(credit.getId())
                .owner(owner)
                .title("Carbon Credit Sale from " + owner.getFullName())
                .description("Verified carbon credits from Journey #" + representativeJourneyId)
                .quantity(request.getAmount())
                .price(request.getPricePerCredit())
                .unit("tCO2")
                .listingType(ListingType.FIXED_PRICE)
                .status(ListingStatus.PENDING)
                .build();

        listingRepository.save(listing);

        return ResponseEntity.ok("Listing created successfully. ID: " + listing.getId());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}