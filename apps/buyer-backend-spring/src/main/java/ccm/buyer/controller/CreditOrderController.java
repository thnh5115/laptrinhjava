package ccm.buyer.controller;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.service.CreditOrderService;
import ccm.buyer.dto.request.UpdateOrderStatusRequest;
import ccm.buyer.dto.response.BuyerDashboardResponse;
import ccm.buyer.dto.response.CreditOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CreditOrderController {

    private final CreditOrderService creditOrderService;

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<CreditOrderResponse>> getOrdersByBuyer(@PathVariable Long buyerId) {
        List<CreditOrder> orders = creditOrderService.getOrdersByBuyer(buyerId);
        List<CreditOrderResponse> responses = orders.stream()
                .map(CreditOrderResponse::of)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<CreditOrderResponse> createOrder(@RequestBody CreditOrder order) {
        CreditOrder created = creditOrderService.createOrder(order);
        return ResponseEntity.ok(CreditOrderResponse.of(created));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CreditOrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        CreditOrder updated = creditOrderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(CreditOrderResponse.of(updated)); // map DTO, không trả entity
    }

    @GetMapping("/dashboard/{buyerId}")
    public ResponseEntity<BuyerDashboardResponse> getBuyerDashboard(@PathVariable Long buyerId) {
        return ResponseEntity.ok(creditOrderService.getBuyerDashboard(buyerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditOrderResponse> getOrderById(@PathVariable Long id) {
        CreditOrder order = creditOrderService.getOrderById(id);
        return ResponseEntity.ok(CreditOrderResponse.of(order));
    }
}
