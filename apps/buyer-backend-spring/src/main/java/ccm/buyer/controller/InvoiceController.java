package ccm.buyer.controller;

import ccm.buyer.entity.Invoice;
import ccm.buyer.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @GetMapping
    public ResponseEntity<List<Invoice>> list(@RequestParam Long buyerId) {
        return ResponseEntity.ok(service.listInvoicesByBuyer(buyerId));
    }
}
