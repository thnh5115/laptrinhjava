package ccm.buyer.service;

import java.math.BigDecimal;

import ccm.buyer.entity.Transaction;

public interface BuyerService {
    Transaction directBuy(Long buyerId, Long listingId, BigDecimal qty);
}
