package ccm.admin.system.settings.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a system configuration setting
 * Settings can be updated at runtime without server restart
 */
@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique key name for the setting (e.g., "carbon_credit_conversion_rate")
     */
    @Column(name = "key_name", unique = true, nullable = false, length = 100)
    private String keyName;

    /**
     * Current value of the setting (stored as string, parsed by application)
     */
    @Column(nullable = false, length = 500)
    private String value;

    /**
     * Human-readable description of what this setting controls
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * When this setting was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Automatically update timestamp before update
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Set initial timestamp if not already set
     */
    @PrePersist
    protected void onCreate() {
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
}
