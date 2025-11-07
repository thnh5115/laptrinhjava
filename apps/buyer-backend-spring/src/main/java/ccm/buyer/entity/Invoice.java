package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long trId;

    private LocalDateTime issueDate;

    private String filePath;

    @PrePersist
    void onCreate() { issueDate = LocalDateTime.now(); }
}
