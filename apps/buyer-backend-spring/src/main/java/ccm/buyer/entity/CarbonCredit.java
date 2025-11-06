package ccm.buyer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "carbon_credits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CarbonCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;
    private String title;
    private String standard;
    private String creditType;
    private LocalDateTime createdAt;
}
