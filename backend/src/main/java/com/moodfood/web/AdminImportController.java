package com.moodfood.web;

import com.moodfood.domain.QualityStatus;
import com.moodfood.domain.Recipe;
import com.moodfood.repository.RecipeImportRepository;
import com.moodfood.repository.RecipeRepository;
import com.moodfood.service.RecipeCollectorService;
import com.moodfood.web.dto.RecipeSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Admin Review (spec §20) — a normalized/tagged recipe never reaches the
 * public catalog until an operator approves it here. */
@RestController
@RequestMapping("/api/admin")
public class AdminImportController {

  private final RecipeCollectorService collectorService;
  private final RecipeImportRepository recipeImportRepository;
  private final RecipeRepository recipeRepository;

  public AdminImportController(RecipeCollectorService collectorService,
                                RecipeImportRepository recipeImportRepository,
                                RecipeRepository recipeRepository) {
    this.collectorService = collectorService;
    this.recipeImportRepository = recipeImportRepository;
    this.recipeRepository = recipeRepository;
  }

  public record ImportRequestDto(String sourceName, String query, int limit) {}

  @PostMapping("/import")
  public RecipeCollectorService.RunSummary runImport(@RequestBody ImportRequestDto request) {
    return collectorService.runImport(request.sourceName(), request.query(), request.limit());
  }

  @GetMapping("/imports")
  public List<ImportSummaryDto> listImports() {
    return recipeImportRepository.findAll().stream()
        .map(i -> new ImportSummaryDto(i.getId(), i.getSourceName(), i.getStatus().name(),
            i.getRecipeId(), i.getErrorMessage()))
        .toList();
  }

  public record ImportSummaryDto(Long id, String sourceName, String status, Long recipeId, String errorMessage) {}

  @GetMapping("/recipes/review")
  public List<RecipeSummaryDto> recipesPendingReview() {
    return recipeRepository.findByQualityStatus(QualityStatus.TAGGED).stream()
        .map(r -> new RecipeSummaryDto(r.getId(), r.getTitle(), r.getImageUrl(), r.getTotalTimeMin(),
            r.getDifficulty() != null ? r.getDifficulty().name() : null, r.getQualityStatus().name()))
        .toList();
  }

  @PostMapping("/recipes/{id}/approve")
  public RecipeSummaryDto approve(@PathVariable Long id) {
    Recipe recipe = recipeRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다."));
    recipe.setQualityStatus(QualityStatus.PUBLISHED);
    recipe = recipeRepository.save(recipe);
    return new RecipeSummaryDto(recipe.getId(), recipe.getTitle(), recipe.getImageUrl(),
        recipe.getTotalTimeMin(), recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null,
        recipe.getQualityStatus().name());
  }

  @PostMapping("/recipes/{id}/reject")
  public RecipeSummaryDto reject(@PathVariable Long id) {
    Recipe recipe = recipeRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다."));
    recipe.setQualityStatus(QualityStatus.ARCHIVED);
    recipe = recipeRepository.save(recipe);
    return new RecipeSummaryDto(recipe.getId(), recipe.getTitle(), recipe.getImageUrl(),
        recipe.getTotalTimeMin(), recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null,
        recipe.getQualityStatus().name());
  }
}
