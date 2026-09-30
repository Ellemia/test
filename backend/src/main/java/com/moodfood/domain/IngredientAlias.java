package com.moodfood.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ingredient_aliases")
public class IngredientAlias {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ingredient_id")
  private Ingredient ingredient;

  @Column(nullable = false, unique = true, length = 100)
  private String alias;

  protected IngredientAlias() {}

  public IngredientAlias(Ingredient ingredient, String alias) {
    this.ingredient = ingredient;
    this.alias = alias;
  }

  public Long getId() { return id; }
  public Ingredient getIngredient() { return ingredient; }
  public String getAlias() { return alias; }
}
