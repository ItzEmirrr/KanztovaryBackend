package kg.kanztovary.kanztovarybackend.config.datasource.entity;

import jakarta.persistence.*;
import kg.kanztovary.kanztovarybackend.domain.enums.DeliveryType;
import kg.kanztovary.kanztovarybackend.domain.enums.OrderStatus;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "orders", schema = "stationery")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;


    /**
     * Сумма товаров без учёта доставки.
     * Итоговая сумма к оплате = totalPrice + deliveryFee.
     */
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Стоимость доставки.
     * 0 — при самовывозе или при сумме заказа выше порога бесплатной доставки.
     */
    @Builder.Default
    @Column(name = "delivery_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 20)
    private DeliveryType deliveryType;

    /**
     * Адрес доставки. Заполняется только при {@code deliveryType = DELIVERY}.
     */
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    /**
     * Номер телефона клиента.
     * Администратор использует его для подтверждения заказа вне зависимости
     * от способа получения.
     */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    /**
     * Позиции заказа.
     * @BatchSize — при загрузке страницы заказов Hibernate подгрузит items
     * пакетами (не N отдельных запросов, а ceil(N/30) запросов).
     */
    @BatchSize(size = 30)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Лог смены статусов.
     * Set вместо List — убирает «bag» и исключает MultipleBagFetchException
     * при совместной загрузке с items через @EntityGraph.
     * @OrderBy гарантирует хронологический порядок из БД.
     */
    @BatchSize(size = 30)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt ASC")
    @Builder.Default
    private Set<OrderStatusHistory> statusHistory = new LinkedHashSet<>();


    /** Итоговая сумма к оплате клиентом (товары + доставка). */
    @Transient
    public BigDecimal getGrandTotal() {
        return totalPrice.add(deliveryFee);
    }
}