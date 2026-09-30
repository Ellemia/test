package com.moodfood.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** One shown recommendation. `context` is the UserContext JSON used to compute it,
 * kept for later algorithm-version comparison/analytics (spec §28-29). */
@Entity
@Table(name = "recommendation_history")
public class RecommendationHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "recipe_id", nullable = false)
  private Long recipeId;

  @Column(name = "algorithm_version", nullable = false, length = 10)
  private String algorithmVersion;

  @Column(nullable = false)
  private BigDecimal score;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private String context;

  @Column(name = "shown_at", nullable = false)
  private Instant shownAt = Instant.now();

  protected RecommendationHistory() {}

  public RecommendationHistory(UUID sessionId, Long recipeId, String algorithmVersion,
                                BigDecimal score, String context) {
    this.sessionId = sessionId;
    this.recipeId = recipeId;
    this.algorithmVersion = algorithmVersion;
    this.score = score;
    this.context = context;
  }

  public Long getId() { return id; }
  public UUID getSessionId() { return sessionId; }
  public Long getRecipeId() { return recipeId; }
  public String getAlgorithmVersion() { return algorithmVersion; }
  public BigDecimal getScore() { return score; }
  public String getContext() { return context; }
  public Instant getShownAt() { return shownAt; }
}
