package ccm.buyer.dto.response;

import ccm.buyer.entity.CreditOrder;
import ccm.buyer.entity.OrderStatus;
import java.time.LocalDateTime;

public class CreditOrderResponse {
    private Long id;
    private Long buyerId;
    private String buyerName;
    private Double amount;
    private Double price;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CreditOrderResponse of(CreditOrder order) {
        CreditOrderResponse r = new CreditOrderResponse();
        r.id = order.getId();
        if (order.getBuyer() != null) {
            r.buyerId = order.getBuyer().getId();
            r.buyerName = order.getBuyer().getFullName();
        }
        r.amount = order.getAmount();
        r.price = order.getPrice();
        r.status = order.getStatus();
        r.createdAt = order.getCreatedAt();
        r.updatedAt = order.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
