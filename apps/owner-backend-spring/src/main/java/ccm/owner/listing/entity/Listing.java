package ccm.owner.listing.entity;

import ccm.admin.user.entity.User; // Sử dụng User từ Admin module (Shared)
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "OwnerListing")
@Table(name = "listings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carbon_credit_id", nullable = false)
    private Long carbonCreditId;

    // --- QUAN TRỌNG: Map vào cột seller_id của Database ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User owner;
    // -----------------------------------------------------

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    // Map đúng cột 'price' trong DB
    @Column(name = "price", nullable = false) 
    private BigDecimal price;

    // Map đúng cột 'quantity' trong DB
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit")
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false)
    private ListingType listingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ListingStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ListingStatus.PENDING;
        if (listingType == null) listingType = ListingType.FIXED_PRICE;
    }
}