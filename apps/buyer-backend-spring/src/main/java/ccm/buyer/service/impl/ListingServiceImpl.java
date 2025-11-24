package ccm.buyer.service.impl;

import ccm.buyer.entity.Listing;
import ccm.buyer.enums.ListingStatus;
import ccm.buyer.repository.ListingRepository;
import ccm.buyer.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;

    @Override
    public List<Listing> getAll() {
        // Chỉ lấy các bài đăng có trạng thái APPROVED (đã duyệt bởi Admin)
        // Hoặc trạng thái OPEN (nếu hệ thống cũ dùng)
        return listingRepository.findAll().stream()
                .filter(l -> l.getStatus() == ListingStatus.APPROVED || l.getStatus() == ListingStatus.OPEN)
                .toList();
    }

    @Override
    public Listing validateOpen(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found: " + id));

        // Kiểm tra: Phải là APPROVED hoặc OPEN mới cho mua
        if (listing.getStatus() != ListingStatus.APPROVED && listing.getStatus() != ListingStatus.OPEN) {
            throw new IllegalStateException("Listing is not available for purchase (Status: " + listing.getStatus() + ")");
        }
        
        // Kiểm tra số lượng
        if (listing.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Listing is out of stock");
        }
        
        return listing;
    }

    @Override
    public void reserve(Long listingId, BigDecimal qty) {
        // Logic giữ chỗ (tạm thời để trống cho demo)
    }

    @Override
    public void release(Long listingId, BigDecimal qty) {
        // Logic nhả chỗ (tạm thời để trống cho demo)
    }
}