package kg.kanztovary.kanztovarybackend.domain.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kg.kanztovary.kanztovarybackend.domain.dto.ai.RecommendationRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.ai.RecommendationResponse;
import kg.kanztovary.kanztovarybackend.domain.dto.ai.RecommendedProduct;
import kg.kanztovary.kanztovarybackend.domain.dto.product.ProductDto;
import kg.kanztovary.kanztovarybackend.domain.dto.product.ProductFilterRequest;
import kg.kanztovary.kanztovarybackend.domain.enums.ProductStatus;
import kg.kanztovary.kanztovarybackend.domain.service.AiRecommendationService;
import kg.kanztovary.kanztovarybackend.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final String MODEL = "claude-opus-4-7";

    private final String SYSTEM_PROMPT = """
            Ты — умный помощник магазина канцелярии. Твоя задача — анализировать каталог товаров
            и рекомендовать наиболее подходящие позиции на основе запроса покупателя.

            Правила:
            1. Анализируй запрос покупателя и выбирай товары только из предоставленного каталога.
            2. Учитывай категорию, бренд, описание и цену товара.
            3. Отдавай предпочтение товарам в наличии (stock > 0).
            4. Возвращай ответ строго в формате JSON, без дополнительного текста:
            {
              "summary": "Краткое пояснение подборки (1-2 предложения)",
              "recommendations": [
                {
                  "productId": 123,
                  "name": "Название товара",
                  "reason": "Почему этот товар подходит"
                }
              ]
            }
            5. Количество рекомендаций — не более указанного maxResults.
            6. Если подходящих товаров нет — верни пустой список и поясни это в summary.
            """;

    private final AnthropicClient anthropicClient;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Override
    public RecommendationResponse recommend(RecommendationRequest request) {
        List<ProductDto> products = fetchActiveProducts();
        String catalog = buildCatalogJson(products);
        String userMessage = buildUserMessage(request, catalog);

        Message response = anthropicClient.messages().create(
                MessageCreateParams.builder()
                        .model(MODEL)
                        .maxTokens(1024L)
                        .systemOfTextBlockParams(List.of(
                                TextBlockParam.builder()
                                        .text(SYSTEM_PROMPT)
                                        .cacheControl(CacheControlEphemeral.builder().build())
                                        .build()
                        ))
                        .addUserMessage(userMessage)
                        .build()
        );

        String rawJson = extractText(response);
        return parseResponse(rawJson);
    }

    private List<ProductDto> fetchActiveProducts() {
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setStatus(ProductStatus.ACTIVE);
        filter.setPage(0);
        filter.setSize(80);
        return productService.search(filter).getProducts();
    }

    private String buildCatalogJson(List<ProductDto> products) {
        try {
            List<Map<String, Object>> compact = new ArrayList<>();
            for (ProductDto p : products) {
                compact.add(Map.of(
                        "id", p.getId(),
                        "name", p.getName(),
                        "description", p.getDescription() != null ? p.getDescription() : "",
                        "price", p.getPrice(),
                        "brand", p.getBrand() != null ? p.getBrand().getName() : "",
                        "categories", p.getCategories() != null
                                ? p.getCategories().stream().map(c -> c.getName()).toList()
                                : List.of(),
                        "stock", p.getStockQuantity()
                ));
            }
            return objectMapper.writeValueAsString(compact);
        } catch (Exception e) {
            log.error("Failed to serialize product catalog", e);
            return "[]";
        }
    }

    private String buildUserMessage(RecommendationRequest request, String catalog) {
        return String.format(
                "Каталог товаров (JSON):\n%s\n\nЗапрос покупателя: %s\n\nМаксимум рекомендаций: %d",
                catalog, request.getQuery(), request.getMaxResults()
        );
    }

    private String extractText(Message message) {
        return message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .findFirst()
                .orElse("{}");
    }

    private RecommendationResponse parseResponse(String rawJson) {
        try {
            String json = rawJson.strip();
            if (json.startsWith("```")) {
                json = json.replaceAll("(?s)^```[a-z]*\\n?", "").replaceAll("```$", "").strip();
            }
            JsonNode root = objectMapper.readTree(json);
            String summary = root.path("summary").asText("");

            List<RecommendedProduct> recs = new ArrayList<>();
            JsonNode recsNode = root.path("recommendations");
            if (recsNode.isArray()) {
                for (JsonNode item : recsNode) {
                    recs.add(RecommendedProduct.builder()
                            .productId(item.path("productId").asLong())
                            .name(item.path("name").asText())
                            .reason(item.path("reason").asText())
                            .build());
                }
            }
            return RecommendationResponse.builder()
                    .summary(summary)
                    .recommendations(recs)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", rawJson, e);
            return RecommendationResponse.builder()
                    .summary("Не удалось обработать ответ от AI.")
                    .recommendations(List.of())
                    .build();
        }
    }
}