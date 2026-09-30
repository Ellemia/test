package com.moodfood.domain;

import jakarta.persistence.*;

/** Service-internal preference/association scores (0-100), not scientific claims
 * about mood/food causality (spec §11). */
@Entity
@Table(name = "recipe_scores")
public class RecipeScores {
  @Id
  @Column(name = "recipe_id")
  private Long recipeId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;

  // Taste
  @Column(name = "taste_spicy") private int tasteSpicy;
  @Column(name = "taste_sweet") private int tasteSweet;
  @Column(name = "taste_salty") private int tasteSalty;
  @Column(name = "taste_sour") private int tasteSour;
  @Column(name = "taste_savory") private int tasteSavory;
  @Column(name = "taste_bitter") private int tasteBitter;
  @Column(name = "taste_rich") private int tasteRich;
  @Column(name = "taste_light") private int tasteLight;

  // Mood/emotional association
  @Column(name = "mood_comfort") private int moodComfort;
  @Column(name = "mood_stress_relief") private int moodStressRelief;
  @Column(name = "mood_happiness") private int moodHappiness;
  @Column(name = "mood_energy") private int moodEnergy;
  @Column(name = "mood_calm") private int moodCalm;
  @Column(name = "mood_nostalgia") private int moodNostalgia;
  @Column(name = "mood_refresh") private int moodRefresh;
  @Column(name = "mood_reward") private int moodReward;

  // Texture / state
  @Column(name = "texture_crispy") private int textureCrispy;
  @Column(name = "texture_soft") private int textureSoft;
  @Column(name = "texture_chewy") private int textureChewy;
  @Column(name = "texture_juicy") private int textureJuicy;
  @Column(name = "texture_warm") private int textureWarm;
  @Column(name = "texture_cool") private int textureCool;
  @Column(name = "texture_hearty") private int textureHearty;
  @Column(name = "texture_light") private int textureLight;

  @Column(name = "tagging_confidence")
  private java.math.BigDecimal taggingConfidence;

  @Column(name = "tagged_by", nullable = false, length = 20)
  private String taggedBy = "RULE";

  protected RecipeScores() {}

  public RecipeScores(Recipe recipe) { this.recipe = recipe; }

  public Recipe getRecipe() { return recipe; }

  public int getTasteSpicy() { return tasteSpicy; }
  public RecipeScores setTasteSpicy(int v) { this.tasteSpicy = clamp(v); return this; }
  public int getTasteSweet() { return tasteSweet; }
  public RecipeScores setTasteSweet(int v) { this.tasteSweet = clamp(v); return this; }
  public int getTasteSalty() { return tasteSalty; }
  public RecipeScores setTasteSalty(int v) { this.tasteSalty = clamp(v); return this; }
  public int getTasteSour() { return tasteSour; }
  public RecipeScores setTasteSour(int v) { this.tasteSour = clamp(v); return this; }
  public int getTasteSavory() { return tasteSavory; }
  public RecipeScores setTasteSavory(int v) { this.tasteSavory = clamp(v); return this; }
  public int getTasteBitter() { return tasteBitter; }
  public RecipeScores setTasteBitter(int v) { this.tasteBitter = clamp(v); return this; }
  public int getTasteRich() { return tasteRich; }
  public RecipeScores setTasteRich(int v) { this.tasteRich = clamp(v); return this; }
  public int getTasteLight() { return tasteLight; }
  public RecipeScores setTasteLight(int v) { this.tasteLight = clamp(v); return this; }

  public int getMoodComfort() { return moodComfort; }
  public RecipeScores setMoodComfort(int v) { this.moodComfort = clamp(v); return this; }
  public int getMoodStressRelief() { return moodStressRelief; }
  public RecipeScores setMoodStressRelief(int v) { this.moodStressRelief = clamp(v); return this; }
  public int getMoodHappiness() { return moodHappiness; }
  public RecipeScores setMoodHappiness(int v) { this.moodHappiness = clamp(v); return this; }
  public int getMoodEnergy() { return moodEnergy; }
  public RecipeScores setMoodEnergy(int v) { this.moodEnergy = clamp(v); return this; }
  public int getMoodCalm() { return moodCalm; }
  public RecipeScores setMoodCalm(int v) { this.moodCalm = clamp(v); return this; }
  public int getMoodNostalgia() { return moodNostalgia; }
  public RecipeScores setMoodNostalgia(int v) { this.moodNostalgia = clamp(v); return this; }
  public int getMoodRefresh() { return moodRefresh; }
  public RecipeScores setMoodRefresh(int v) { this.moodRefresh = clamp(v); return this; }
  public int getMoodReward() { return moodReward; }
  public RecipeScores setMoodReward(int v) { this.moodReward = clamp(v); return this; }

  public int getTextureCrispy() { return textureCrispy; }
  public RecipeScores setTextureCrispy(int v) { this.textureCrispy = clamp(v); return this; }
  public int getTextureSoft() { return textureSoft; }
  public RecipeScores setTextureSoft(int v) { this.textureSoft = clamp(v); return this; }
  public int getTextureChewy() { return textureChewy; }
  public RecipeScores setTextureChewy(int v) { this.textureChewy = clamp(v); return this; }
  public int getTextureJuicy() { return textureJuicy; }
  public RecipeScores setTextureJuicy(int v) { this.textureJuicy = clamp(v); return this; }
  public int getTextureWarm() { return textureWarm; }
  public RecipeScores setTextureWarm(int v) { this.textureWarm = clamp(v); return this; }
  public int getTextureCool() { return textureCool; }
  public RecipeScores setTextureCool(int v) { this.textureCool = clamp(v); return this; }
  public int getTextureHearty() { return textureHearty; }
  public RecipeScores setTextureHearty(int v) { this.textureHearty = clamp(v); return this; }
  public int getTextureLight() { return textureLight; }
  public RecipeScores setTextureLight(int v) { this.textureLight = clamp(v); return this; }

  public java.math.BigDecimal getTaggingConfidence() { return taggingConfidence; }
  public RecipeScores setTaggingConfidence(java.math.BigDecimal v) { this.taggingConfidence = v; return this; }
  public String getTaggedBy() { return taggedBy; }
  public RecipeScores setTaggedBy(String v) { this.taggedBy = v; return this; }

  /** moodFit per §13: recipe's score for a single given mood dimension, 0-100. */
  public int moodScoreFor(Mood mood) {
    return switch (mood) {
      case GOOD -> moodHappiness;
      case NEUTRAL -> (moodComfort + moodCalm) / 2;
      case TIRED -> (moodComfort + moodEnergy) / 2;
      case STRESSED -> moodStressRelief;
      case SAD -> (moodComfort + moodNostalgia) / 2;
      case EXCITED -> (moodHappiness + moodReward) / 2;
      case DRAINED -> moodEnergy;
    };
  }

  /** tasteFit per §13: recipe's score for a single desired-taste dimension, 0-100. */
  public int tasteScoreFor(DesiredTaste taste) {
    return switch (taste) {
      case SPICY -> tasteSpicy;
      case WARM -> textureWarm;
      case HEARTY -> textureHearty;
      case FAMILIAR -> moodNostalgia;
      case LIGHT -> (tasteLight + textureLight) / 2;
      case SWEET -> tasteSweet;
      case SPECIAL -> moodReward;
      case SURPRISE_ME -> 50; // neutral — diversity handles this case, not taste fit
    };
  }

  private static int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}
