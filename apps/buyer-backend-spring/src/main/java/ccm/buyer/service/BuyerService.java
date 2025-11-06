package ccm.buyer.service;

import ccm.buyer.entity.Transaction;

public interface BuyerService {
    Transaction directBuy(Long buyerId, Long listingId, Integer qty);
}
