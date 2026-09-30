package com.moodfood.service;

import com.moodfood.collector.NormalizedRecipe;
import com.moodfood.domain.*;
import com.moodfood.repository.RecipeImportRepository;
import com.moodfood.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Turns one staged {@link RecipeImport} row into a published {@link Recipe},
 * running normalize → dedupe → tag in order (spec's collector pipeline,
 * §6-§11). Exact-source-id duplicates are marked DUPLICATE and skipped;
 * same-title-different-source matches go to REVIEW instead of being merged
 * or dropped automatically (spec §10: "자동 삭제하지 말고 관리자 승인 방식").
 */
@Service
public class RecipeIngestionService {

  private final RecipeRepository recipeRepository;
  private final RecipeImportRepository recipeImportRepository;
  private final DeduplicationService deduplicationService;
  private final IngredientNormalizationService ingredientNormalizationService;
  private final TaggingService taggingService;

  public RecipeIngestionService(RecipeRepository recipeRepository,
                                 RecipeImportRepository recipeImportRepository,
                                 DeduplicationService deduplicationService,
                                 IngredientNormalizationService ingredientNormalizationService,
                                 TaggingService taggingService) {
    this.recipeRepository = recipeRepository;
    this.recipeImportRepository = recipeImportRepository;
    this.deduplicationService = deduplicationService;
    this.ingredientNormalizationService = ingredientNormalizationService;
    this.taggingService = taggingService;
  }

  @Transactional
  public IngestResult ingest(RecipeImport recipeImport, NormalizedRecipe normalized) {
    Optional<Recipe> exactMatch = deduplicationService.findExactSourceMatch(
        recipeImport.getSourceType(), recipeImport.getSourceName(), recipeImport.getExternalRecipeId());
    if (exactMatch.isPresent()) {
      recipeImport.markDuplicate(exactMatch.get().getId());
      return IngestResult.duplicate(exactMatch.get());
    }

    List<Recipe> titleCandidates = deduplicationService.findTitleCandidates(normalized.title());
    if (!titleCandidates.isEmpty()) {
      recipeImport.markReview("Possible duplicate of recipe id(s): "
          + titleCandidates.stream().map(r -> String.valueOf(r.getId())).reduce((a, b) -> a + "," + b).orElse(""));
      return IngestResult.review(titleCandidates);
    }

    Recipe recipe = buildRecipe(recipeImport, normalized);
    recipe = recipeRepository.save(recipe);
    recipe.setQualityStatus(QualityStatus.NORMALIZED);

    RecipeScores scores = taggingService.tag(recipe);
    recipe.setScores(scores);
    recipe.setQualityStatus(QualityStatus.TAGGED);

    recipeImport.markNormalized(recipe.getId());
    return IngestResult.created(recipe);
  }

  private Recipe buildRecipe(RecipeImport recipeImport, NormalizedRecipe normalized) {
    Recipe recipe = new Recipe(
        normalized.title(),
        recipeImport.getSourceType(),
        recipeImport.getSourceName(),
        recipeImport.getLicense(),
        recipeImport.isAttributionRequired(),
        recipeImport.getRetrievedAt() != null ? recipeImport.getRetrievedAt() : Instant.now()
    );
    recipe.setDescription(normalized.description());
    recipe.setServings(normalized.servings());
    recipe.setPrepTimeMin(normalized.prepTimeMin());
    recipe.setCookTimeMin(normalized.cookTimeMin());
    recipe.setTotalTimeMin(normalized.totalTimeMin());
    recipe.setDifficulty(normalized.difficulty());
    recipe.setCategory(normalized.category());
    recipe.setCuisine(normalized.cuisine());
    recipe.setImageUrl(normalized.imageUrl());
    recipe.setSourceUrl(recipeImport.getSourceUrl());
    recipe.setSourceRecipeId(recipeImport.getExternalRecipeId());

    int position = 0;
    for (NormalizedRecipe.NormalizedIngredient ni : normalized.ingredients()) {
      Ingredient ingredient = ingredientNormalizationService.resolve(ni.rawText());
      recipe.addIngredient(new RecipeIngredient(ni.rawText(), ingredient,
          ni.quantity(), ni.unit(), position++));
    }

    int stepNumber = 1;
    for (String instruction : normalized.steps()) {
      recipe.addStep(new RecipeStep(stepNumber++, instruction));
    }

    for (String tag : normalized.tags()) {
      recipe.addTag(tag);
    }

    return recipe;
  }

  public sealed interface IngestResult permits IngestResult.Created, IngestResult.Duplicate, IngestResult.Review {
    record Created(Recipe recipe) implements IngestResult {}
    record Duplicate(Recipe existing) implements IngestResult {}
    record Review(List<Recipe> candidates) implements IngestResult {}

    static IngestResult created(Recipe recipe) { return new Created(recipe); }
    static IngestResult duplicate(Recipe existing) { return new Duplicate(existing); }
    static IngestResult review(List<Recipe> candidates) { return new Review(candidates); }
  }
}
