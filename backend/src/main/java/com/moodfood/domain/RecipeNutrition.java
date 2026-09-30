package com.moodfood.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recipe_nutrition")
public class RecipeNutrition {
  @Id
  @Column(name = "recipe_id")
  private Long recipeId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;

  private BigDecimal calories;

  @Column(name = "protein_g")
  private BigDecimal proteinG;

  @Column(name = "fat_g")
  private BigDecimal fatG;

  @Column(name = "carbs_g")
  private BigDecimal carbsG;

  @Column(name = "sodium_mg")
  private BigDecimal sodiumMg;

  @Column(length = 100)
  private String source;

  protected RecipeNutrition() {}

  public RecipeNutrition(Recipe recipe, BigDecimal calories, BigDecimal proteinG,
                          BigDecimal fatG, BigDecimal carbsG, BigDecimal sodiumMg, String source) {
    this.recipe = recipe;
    this.calories = calories;
    this.proteinG = proteinG;
    this.fatG = fatG;
    this.carbsG = carbsG;
    this.sodiumMg = sodiumMg;
    this.source = source;
  }

  public Long getRecipeId() { return recipeId; }
  public BigDecimal getCalories() { return calories; }
  public BigDecimal getProteinG() { return proteinG; }
  public BigDecimal getFatG() { return fatG; }
  public BigDecimal getCarbsG() { return carbsG; }
  public BigDecimal getSodiumMg() { return sodiumMg; }
  public String getSource() { return source; }
}
