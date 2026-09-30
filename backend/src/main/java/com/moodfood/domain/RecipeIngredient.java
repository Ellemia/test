package com.moodfood.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id")
  private Ingredient ingredient;

  @Column(name = "raw_text", nullable = false, length = 200)
  private String rawText;

  private BigDecimal quantity;

  @Column(length = 20)
  private String unit;

  @Column(nullable = false)
  private int position;

  protected RecipeIngredient() {}

  public RecipeIngredient(String rawText, Ingredient ingredient, BigDecimal quantity, String unit, int position) {
    this.rawText = rawText;
    this.ingredient = ingredient;
    this.quantity = quantity;
    this.unit = unit;
    this.position = position;
  }

  public Long getId() { return id; }
  public Recipe getRecipe() { return recipe; }
  public void setRecipe(Recipe recipe) { this.recipe = recipe; }
  public Ingredient getIngredient() { return ingredient; }
  public String getRawText() { return rawText; }
  public BigDecimal getQuantity() { return quantity; }
  public String getUnit() { return unit; }
  public int getPosition() { return position; }
}
