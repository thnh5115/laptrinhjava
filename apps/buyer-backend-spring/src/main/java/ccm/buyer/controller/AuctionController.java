package ccm.buyer.controller;

import ccm.buyer.entity.Bid;
import ccm.buyer.service.AuctionService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buyer/bid")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService service;

    @PostMapping
    public ResponseEntity<Bid> placeBid(
            @RequestParam Long buyerId,
            @RequestParam Long auctionId,
            @RequestParam BigDecimal bidPrice
    ) {
        return ResponseEntity.ok(service.placeBid(buyerId, auctionId, bidPrice));
    }
}
