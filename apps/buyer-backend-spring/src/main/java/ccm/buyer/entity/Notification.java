package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long buyerId;

    private String message;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
