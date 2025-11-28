package ccm.buyer.service;

import ccm.buyer.dto.request.CreateTransactionRequest;
import ccm.buyer.dto.response.TransactionResponse;
import ccm.buyer.enums.TrStatus;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
  List<TransactionResponse> list(Long buyerId);
  TransactionResponse create(CreateTransactionRequest req);
  TransactionResponse updateStatus(Long id, TrStatus status);
  TransactionResponse buyListing(Long buyerId, Long listingId, BigDecimal qtyRequested) ;
}
