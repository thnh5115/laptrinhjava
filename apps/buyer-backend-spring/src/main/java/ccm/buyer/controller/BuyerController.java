package ccm.buyer.controller;

import ccm.buyer.dto.request.CreateBuyerRequest;
import ccm.buyer.dto.response.BuyerResponse;
import ccm.buyer.entity.Buyer;
import ccm.buyer.service.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @GetMapping
    public ResponseEntity<List<Buyer>> getAllBuyers() {
        return ResponseEntity.ok(buyerService.getAllBuyers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Buyer> getBuyerById(@PathVariable Long id) {
        return buyerService.getBuyerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BuyerResponse> createBuyer(@Valid @RequestBody CreateBuyerRequest request) {
        Buyer buyer = Buyer.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(request.getPassword())
                .status(request.getStatus())
                .build();

        Buyer saved = buyerService.createBuyer(buyer);
        return ResponseEntity.ok(BuyerResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .status(saved.getStatus())
                .build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Buyer> updateBuyer(@PathVariable("id") Long id, @RequestBody Buyer buyer) {
        return ResponseEntity.ok(buyerService.updateBuyer(id, buyer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuyer(@PathVariable("id") Long id) {
        buyerService.deleteBuyer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BuyerResponse>> searchBuyers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Buyer> buyers = buyerService.getBuyers(keyword, pageable);
        Page<BuyerResponse> response = buyers.map(b -> BuyerResponse.builder()
                .id(b.getId())
                .email(b.getEmail())
                .fullName(b.getFullName())
                .status(b.getStatus())
                .build());
        return ResponseEntity.ok(response);
    }
}
