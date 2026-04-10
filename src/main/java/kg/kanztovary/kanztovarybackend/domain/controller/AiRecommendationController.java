package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.domain.dto.ai.RecommendationRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.ai.RecommendationResponse;
import kg.kanztovary.kanztovarybackend.domain.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping
    public RecommendationResponse recommend(@Valid @RequestBody RecommendationRequest request) {
        return aiRecommendationService.recommend(request);
    }
}