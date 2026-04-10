package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.ai.RecommendationRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.ai.RecommendationResponse;

public interface AiRecommendationService {

    /** Подбирает товары на основе запроса пользователя*/
    RecommendationResponse recommend(RecommendationRequest request);
}