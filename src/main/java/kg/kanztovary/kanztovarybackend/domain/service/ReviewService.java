package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.domain.dto.review.CreateReviewRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.review.ReviewDto;
import kg.kanztovary.kanztovarybackend.domain.dto.review.ReviewStatsDto;
import kg.kanztovary.kanztovarybackend.domain.dto.review.UpdateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /** Оставить отзыв (1 отзыв на пользователя) */
    ReviewDto create(User user, CreateReviewRequest request);

    /** Обновить свой отзыв */
    ReviewDto update(User user, Long reviewId, UpdateReviewRequest request);

    /** Удалить отзыв (автор или ADMIN) */
    void delete(User user, Long reviewId);

    /** Список всех отзывов с пагинацией */
    Page<ReviewDto> getAll(Pageable pageable);

    /** Статистика: средняя оценка + распределение */
    ReviewStatsDto getStats();

    /** Мой отзыв (авторизованный пользователь) */
    ReviewDto getMyReview(User user);
}