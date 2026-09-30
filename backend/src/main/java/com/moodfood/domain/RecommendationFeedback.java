package com.moodfood.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation_feedback")
public class RecommendationFeedback {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "recommendation_history_id")
  private Long recommendationHistoryId;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "recipe_id", nullable = false)
  private Long recipeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FeedbackType feedback;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected RecommendationFeedback() {}

  public RecommendationFeedback(Long recommendationHistoryId, UUID sessionId, Long recipeId, FeedbackType feedback) {
    this.recommendationHistoryId = recommendationHistoryId;
    this.sessionId = sessionId;
    this.recipeId = recipeId;
    this.feedback = feedback;
  }

  public Long getId() { return id; }
  public Long getRecommendationHistoryId() { return recommendationHistoryId; }
  public UUID getSessionId() { return sessionId; }
  public Long getRecipeId() { return recipeId; }
  public FeedbackType getFeedback() { return feedback; }
  public Instant getCreatedAt() { return createdAt; }
}
