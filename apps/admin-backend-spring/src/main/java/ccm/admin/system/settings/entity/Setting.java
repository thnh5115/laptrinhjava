package ccm.admin.system.settings.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** entity - Entity - JPA entity for entity table */

public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "key_name", unique = true, nullable = false, length = 100)
    private String keyName;

    
    @Column(nullable = false, length = 500)
    private String value;

    
    @Column(columnDefinition = "TEXT")
    private String description;

    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    
    @PrePersist
    protected void onCreate() {
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
}
