package ccm.buyer.controller;
import ccm.buyer.enums.TrStatus;
import ccm.buyer.dto.request.UpdateOrderStatusRequest;
import ccm.buyer.dto.response.BuyerDashboardResponse;
import ccm.buyer.dto.response.CreditOrderResponse;

import ccm.buyer.service.CreditOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer/orders")
@RequiredArgsConstructor
public class CreditOrderController {

    private final CreditOrderService creditOrderService;

    @GetMapping("/dashboard/{buyerId}")
    public ResponseEntity<BuyerDashboardResponse> dashboard(@PathVariable Long buyerId) {
        return ResponseEntity.ok(creditOrderService.getBuyerDashboard(buyerId));
    }

    @GetMapping
    public ResponseEntity<Page<CreditOrderResponse>> list(
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(creditOrderService.list(buyerId, status, pageable));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CreditOrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        TrStatus newStatus = request.getStatus() != null
                ? request.getStatus()
                : TrStatus.valueOf("PENDING");
        return ResponseEntity.ok(creditOrderService.updateStatus(id, newStatus));
    }
}
