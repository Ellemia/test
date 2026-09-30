package com.moodfood.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_tags")
public class RecipeTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;

  @Column(nullable = false, length = 50)
  private String tag;

  protected RecipeTag() {}

  public RecipeTag(Recipe recipe, String tag) {
    this.recipe = recipe;
    this.tag = tag;
  }

  public Long getId() { return id; }
  public Recipe getRecipe() { return recipe; }
  public String getTag() { return tag; }
}
