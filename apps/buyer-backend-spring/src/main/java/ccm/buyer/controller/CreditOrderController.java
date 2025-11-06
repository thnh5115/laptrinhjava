package ccm.buyer.controller;


import ccm.buyer.dto.request.UpdateOrderStatusRequest;
import ccm.buyer.dto.response.BuyerDashboardResponse;
import ccm.buyer.dto.response.CreditOrderResponse;
import ccm.buyer.entity.CreditOrder;
import ccm.buyer.service.CreditOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CreditOrderController {

    private final CreditOrderService creditOrderService;

    @GetMapping("/{id}")
    public ResponseEntity<CreditOrderResponse> getOrderById(@PathVariable Long id) {
        CreditOrder order = creditOrderService.getOrderById(id);
        return ResponseEntity.ok(CreditOrderResponse.of(order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CreditOrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        CreditOrder updated = creditOrderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(CreditOrderResponse.of(updated));
    }

    @GetMapping("/dashboard/{buyerId}")
    public ResponseEntity<BuyerDashboardResponse> getBuyerDashboard(@PathVariable Long buyerId) {
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

}
