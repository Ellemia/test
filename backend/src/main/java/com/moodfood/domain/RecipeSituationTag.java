package com.moodfood.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_situations")
public class RecipeSituationTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Situation situation;

  protected RecipeSituationTag() {}

  public RecipeSituationTag(Recipe recipe, Situation situation) {
    this.recipe = recipe;
    this.situation = situation;
  }

  public Long getId() { return id; }
  public Recipe getRecipe() { return recipe; }
  public Situation getSituation() { return situation; }
}
