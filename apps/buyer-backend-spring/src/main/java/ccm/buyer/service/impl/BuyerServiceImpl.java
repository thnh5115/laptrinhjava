package ccm.buyer.service.impl;

import ccm.buyer.entity.Buyer;
import ccm.buyer.repository.BuyerRepository;
import ccm.buyer.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepository;

    @Override
    public List<Buyer> getAllBuyers() {
        return buyerRepository.findAll();
    }

    @Override
    public Page<Buyer> getBuyers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return buyerRepository.findAll(pageable);
        }
        return buyerRepository.findByFullNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Optional<Buyer> getBuyerById(Long id) {
        return buyerRepository.findById(id);
    }

    @Override
    public Buyer createBuyer(Buyer buyer) { 
        return buyerRepository.save(buyer);
    }

    @Override
    public Buyer updateBuyer(Long id, Buyer buyer) {
        Buyer existing = buyerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        existing.setEmail(buyer.getEmail());
        existing.setFullName(buyer.getFullName());
        existing.setPassword(buyer.getPassword());
        existing.setStatus(buyer.getStatus());
        return buyerRepository.save(existing);
    }

    @Override
    public void deleteBuyer(Long id) {
        buyerRepository.deleteById(id);
    }
}
