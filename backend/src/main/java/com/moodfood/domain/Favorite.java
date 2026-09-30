package com.moodfood.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "favorites")
public class Favorite {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "recipe_id", nullable = false)
  private Long recipeId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected Favorite() {}

  public Favorite(UUID sessionId, Long recipeId) {
    this.sessionId = sessionId;
    this.recipeId = recipeId;
  }

  public Long getId() { return id; }
  public UUID getSessionId() { return sessionId; }
  public Long getRecipeId() { return recipeId; }
  public Instant getCreatedAt() { return createdAt; }
}
