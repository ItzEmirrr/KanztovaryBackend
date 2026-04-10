package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Review;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.ReviewRepository;
import kg.kanztovary.kanztovarybackend.domain.dto.review.CreateReviewRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.review.ReviewDto;
import kg.kanztovary.kanztovarybackend.domain.dto.review.ReviewStatsDto;
import kg.kanztovary.kanztovarybackend.domain.dto.review.UpdateReviewRequest;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.enums.Roles;
import kg.kanztovary.kanztovarybackend.domain.service.ReviewService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ReviewDto create(User user, CreateReviewRequest request) {
        if (reviewRepository.existsByUserId(user.getId())) {
            throw new ResponseException(
                    ResponseStatus.ALREADY_REVIEWED.getCode(),
                    ResponseStatus.ALREADY_REVIEWED.getMessage()
            );
        }

        Review review = Review.builder()
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
        log.info("Пользователь {} оставил отзыв с оценкой {}", user.getEmail(), request.getRating());
        return toDto(review);
    }

    @Override
    @Transactional
    public ReviewDto update(User user, Long reviewId, UpdateReviewRequest request) {
        Review review = findById(reviewId);
        checkOwnership(user, review);

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        log.info("Пользователь {} обновил отзыв #{}", user.getEmail(), reviewId);
        return toDto(review);
    }

    @Override
    @Transactional
    public void delete(User user, Long reviewId) {
        Review review = findById(reviewId);

        if (!review.getUser().getId().equals(user.getId()) && user.getRole() != Roles.ADMIN) {
            throw new ResponseException(
                    ResponseStatus.REVIEW_ACCESS_DENIED.getCode(),
                    ResponseStatus.REVIEW_ACCESS_DENIED.getMessage()
            );
        }

        reviewRepository.delete(review);
        log.info("Отзыв #{} удалён пользователем {}", reviewId, user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAll(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsDto getStats() {
        long total = reviewRepository.count();
        double avg = total == 0 ? 0.0 : reviewRepository.findAverageRating();

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);

        List<Object[]> rows = reviewRepository.findRatingDistribution();
        for (Object[] row : rows) {
            int star = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            distribution.put(star, count);
        }

        return ReviewStatsDto.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews(total)
                .distribution(distribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getMyReview(User user) {
        Review review = reviewRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.REVIEW_NOT_FOUND.getCode(),
                        ResponseStatus.REVIEW_NOT_FOUND.getMessage()
                ));
        return toDto(review);
    }


    private Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.REVIEW_NOT_FOUND.getCode(),
                        ResponseStatus.REVIEW_NOT_FOUND.getMessage()
                ));
    }

    private void checkOwnership(User user, Review review) {
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResponseException(
                    ResponseStatus.REVIEW_ACCESS_DENIED.getCode(),
                    ResponseStatus.REVIEW_ACCESS_DENIED.getMessage()
            );
        }
    }

    private ReviewDto toDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}