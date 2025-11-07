package ccm.buyer.repository;

import ccm.buyer.entity.Buyer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {}
