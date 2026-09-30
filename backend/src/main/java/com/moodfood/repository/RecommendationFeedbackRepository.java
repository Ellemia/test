package com.moodfood.repository;

import com.moodfood.domain.RecommendationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {
  List<RecommendationFeedback> findBySessionId(UUID sessionId);
  List<RecommendationFeedback> findBySessionIdAndRecipeId(UUID sessionId, Long recipeId);
}
