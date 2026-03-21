package kg.kanztovary.kanztovarybackend.domain.dto.order;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusHistoryDto {
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
}