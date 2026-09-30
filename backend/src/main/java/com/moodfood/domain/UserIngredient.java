package com.moodfood.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_ingredients")
public class UserIngredient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ingredient_id")
  private Ingredient ingredient;

  protected UserIngredient() {}

  public UserIngredient(UUID sessionId, Ingredient ingredient) {
    this.sessionId = sessionId;
    this.ingredient = ingredient;
  }

  public Long getId() { return id; }
  public UUID getSessionId() { return sessionId; }
  public Ingredient getIngredient() { return ingredient; }
}
