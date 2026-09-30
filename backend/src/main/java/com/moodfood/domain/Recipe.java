package com.moodfood.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  private Integer servings;

  @Column(name = "prep_time_min")
  private Integer prepTimeMin;

  @Column(name = "cook_time_min")
  private Integer cookTimeMin;

  @Column(name = "total_time_min")
  private Integer totalTimeMin;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Difficulty difficulty;

  @Column(length = 30)
  private String category;

  @Column(length = 30)
  private String cuisine;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 30)
  private SourceType sourceType;

  @Column(name = "source_name", nullable = false, length = 100)
  private String sourceName;

  @Column(name = "source_url", length = 500)
  private String sourceUrl;

  @Column(name = "source_recipe_id", length = 100)
  private String sourceRecipeId;

  @Column(nullable = false, length = 100)
  private String license;

  @Column(name = "attribution_required", nullable = false)
  private boolean attributionRequired;

  @Column(name = "retrieved_at", nullable = false)
  private Instant retrievedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "quality_status", nullable = false, length = 20)
  private QualityStatus qualityStatus = QualityStatus.RAW;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position ASC")
  private List<RecipeIngredient> ingredients = new ArrayList<>();

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("stepNumber ASC")
  private List<RecipeStep> steps = new ArrayList<>();

  @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private RecipeNutrition nutrition;

  @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private RecipeScores scores;

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RecipeSituationTag> situations = new ArrayList<>();

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RecipeTag> tags = new ArrayList<>();

  protected Recipe() {}

  public Recipe(String title, SourceType sourceType, String sourceName, String license,
                boolean attributionRequired, Instant retrievedAt) {
    this.title = title;
    this.sourceType = sourceType;
    this.sourceName = sourceName;
    this.license = license;
    this.attributionRequired = attributionRequired;
    this.retrievedAt = retrievedAt;
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public Integer getServings() { return servings; }
  public void setServings(Integer servings) { this.servings = servings; }
  public Integer getPrepTimeMin() { return prepTimeMin; }
  public void setPrepTimeMin(Integer v) { this.prepTimeMin = v; }
  public Integer getCookTimeMin() { return cookTimeMin; }
  public void setCookTimeMin(Integer v) { this.cookTimeMin = v; }
  public Integer getTotalTimeMin() { return totalTimeMin; }
  public void setTotalTimeMin(Integer v) { this.totalTimeMin = v; }
  public Difficulty getDifficulty() { return difficulty; }
  public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getCuisine() { return cuisine; }
  public void setCuisine(String cuisine) { this.cuisine = cuisine; }
  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  public SourceType getSourceType() { return sourceType; }
  public String getSourceName() { return sourceName; }
  public String getSourceUrl() { return sourceUrl; }
  public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
  public String getSourceRecipeId() { return sourceRecipeId; }
  public void setSourceRecipeId(String v) { this.sourceRecipeId = v; }
  public String getLicense() { return license; }
  public boolean isAttributionRequired() { return attributionRequired; }
  public Instant getRetrievedAt() { return retrievedAt; }
  public QualityStatus getQualityStatus() { return qualityStatus; }
  public void setQualityStatus(QualityStatus qualityStatus) {
    this.qualityStatus = qualityStatus;
    this.updatedAt = Instant.now();
  }
  public List<RecipeIngredient> getIngredients() { return ingredients; }
  public List<RecipeStep> getSteps() { return steps; }
  public RecipeNutrition getNutrition() { return nutrition; }
  public void setNutrition(RecipeNutrition nutrition) { this.nutrition = nutrition; }
  public RecipeScores getScores() { return scores; }
  public void setScores(RecipeScores scores) { this.scores = scores; }
  public List<RecipeSituationTag> getSituations() { return situations; }
  public List<RecipeTag> getTags() { return tags; }

  public void addIngredient(RecipeIngredient ri) {
    ri.setRecipe(this);
    this.ingredients.add(ri);
  }

  public void addStep(RecipeStep step) {
    step.setRecipe(this);
    this.steps.add(step);
  }

  public void addSituation(Situation situation) {
    RecipeSituationTag t = new RecipeSituationTag(this, situation);
    this.situations.add(t);
  }

  public void addTag(String tag) {
    this.tags.add(new RecipeTag(this, tag));
  }
}
