package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.domain.dto.review.CreateReviewRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.review.ReviewDto;
import kg.kanztovary.kanztovarybackend.domain.dto.review.ReviewStatsDto;
import kg.kanztovary.kanztovarybackend.domain.dto.review.UpdateReviewRequest;
import kg.kanztovary.kanztovarybackend.domain.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return reviewService.create(user, request);
    }

    @PutMapping("/{id}")
    public ReviewDto update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return reviewService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        reviewService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<ReviewDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reviewService.getAll(PageRequest.of(page, size));
    }

    @GetMapping("/stats")
    public ReviewStatsDto getStats() {
        return reviewService.getStats();
    }

    @GetMapping("/me")
    public ReviewDto getMyReview(@AuthenticationPrincipal User user) {
        return reviewService.getMyReview(user);
    }
}