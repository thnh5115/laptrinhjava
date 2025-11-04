package ccm.buyer.controller;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.service.CreditOrderService;
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
    public ResponseEntity<List<CreditOrder>> getOrdersByBuyer(@PathVariable("buyerId") Long buyerId) {
        return ResponseEntity.ok(creditOrderService.getOrdersByBuyer(buyerId));
    }

    @PostMapping
    public ResponseEntity<CreditOrder> createOrder(@RequestBody CreditOrder order) {
        return ResponseEntity.ok(creditOrderService.createOrder(order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CreditOrder> updateStatus(@PathVariable("id") Long id,
                                                    @RequestParam String status) {
        return ResponseEntity.ok(creditOrderService.updateStatus(id, status));
    }
}
