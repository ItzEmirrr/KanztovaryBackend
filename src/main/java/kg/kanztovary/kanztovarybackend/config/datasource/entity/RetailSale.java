package kg.kanztovary.kanztovarybackend.config.datasource.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "retail_sales", schema = "stationery")
public class RetailSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Администратор, проводивший продажу */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    /** Необязательная заметка (имя покупателя, номер чека и т.д.) */
    @Column(length = 500)
    private String note;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @BatchSize(size = 20)
    @Builder.Default
    @OneToMany(mappedBy = "retailSale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetailSaleItem> items = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}