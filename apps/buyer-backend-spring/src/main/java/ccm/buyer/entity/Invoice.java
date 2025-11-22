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

    @Column(name = "transaction_id", nullable = false, unique = true) // Map trId -> transaction_id
    private Long trId;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "file_path", nullable = false) // Map filePath -> file_path
    private String filePath;

    @PrePersist
    void onCreate() { issueDate = LocalDateTime.now(); }
}