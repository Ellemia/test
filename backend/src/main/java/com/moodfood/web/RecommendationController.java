package com.moodfood.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodfood.domain.FeedbackType;
import com.moodfood.domain.RecommendationHistory;
import com.moodfood.repository.RecommendationHistoryRepository;
import com.moodfood.service.*;
import com.moodfood.web.dto.RecommendRequestDto;
import com.moodfood.web.dto.RecommendResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

  private final RecommendationEngine engine;
  private final SessionService sessionService;
  private final RecommendationHistoryRepository historyRepository;
  private final FeedbackService feedbackService;
  private final ObjectMapper objectMapper;

  public RecommendationController(RecommendationEngine engine, SessionService sessionService,
                                   RecommendationHistoryRepository historyRepository,
                                   FeedbackService feedbackService, ObjectMapper objectMapper) {
    this.engine = engine;
    this.sessionService = sessionService;
    this.historyRepository = historyRepository;
    this.feedbackService = feedbackService;
    this.objectMapper = objectMapper;
  }

  @PostMapping
  public RecommendResponseDto recommend(@RequestHeader("X-Session-Id") UUID sessionId,
                                         @Valid @RequestBody RecommendRequestDto request) {
    sessionService.ensure(sessionId);
    UserContext context = new UserContext(request.mood(), request.desiredTaste(), request.situation(),
        request.cookingWillingness(), request.timeAvailableMin(), request.budgetWon());

    ScoredRecipe scored = engine.recommendOne(sessionId, context)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "추천할 수 있는 레시피가 아직 없습니다. 관리자 검수를 거친 레시피가 필요합니다."));

    String contextJson;
    try {
      contextJson = objectMapper.writeValueAsString(context);
    } catch (Exception e) {
      contextJson = "{}";
    }

    RecommendationHistory history = historyRepository.save(new RecommendationHistory(
        sessionId, scored.recipe().getId(), RecommendationEngine.ALGORITHM_VERSION, scored.finalScore(), contextJson));

    var recipe = scored.recipe();
    return new RecommendResponseDto(
        history.getId(), recipe.getId(), recipe.getTitle(), recipe.getImageUrl(), scored.reason(),
        recipe.getServings(), recipe.getTotalTimeMin(),
        recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null,
        scored.finalScore().doubleValue(),
        new RecommendResponseDto.ScoreBreakdown(scored.moodFit(), scored.tasteFit(), scored.situationFit(),
            scored.cookingFit(), scored.timeFit(), scored.budgetFit(), scored.novelty())
    );
  }

  public record FeedbackRequestDto(FeedbackType feedback, Long recipeId) {}

  @PostMapping("/{recommendationId}/feedback")
  public ResponseEntity<Void> feedback(@RequestHeader("X-Session-Id") UUID sessionId,
                                        @PathVariable Long recommendationId,
                                        @RequestBody FeedbackRequestDto request) {
    Long recipeId = request.recipeId();
    if (recipeId == null) {
      recipeId = historyRepository.findById(recommendationId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 이력을 찾을 수 없습니다."))
          .getRecipeId();
    }
    feedbackService.record(recommendationId, sessionId, recipeId, request.feedback());
    return ResponseEntity.noContent().build();
  }
}
