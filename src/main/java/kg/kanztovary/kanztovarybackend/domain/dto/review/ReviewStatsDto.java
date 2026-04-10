package kg.kanztovary.kanztovarybackend.domain.dto.review;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ReviewStatsDto {

    private double averageRating;
    private long totalReviews;

    /** Количество отзывов по каждой оценке: ключ — звезда (1–5), значение — количество */
    private Map<Integer, Long> distribution;
}