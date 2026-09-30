package com.moodfood.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_steps")
public class RecipeStep {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;

  @Column(name = "step_number", nullable = false)
  private int stepNumber;

  @Column(nullable = false, columnDefinition = "text")
  private String instruction;

  protected RecipeStep() {}

  public RecipeStep(int stepNumber, String instruction) {
    this.stepNumber = stepNumber;
    this.instruction = instruction;
  }

  public Long getId() { return id; }
  public Recipe getRecipe() { return recipe; }
  public void setRecipe(Recipe recipe) { this.recipe = recipe; }
  public int getStepNumber() { return stepNumber; }
  public String getInstruction() { return instruction; }
}
