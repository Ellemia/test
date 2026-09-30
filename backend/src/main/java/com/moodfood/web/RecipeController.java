package com.moodfood.web;

import com.moodfood.domain.Recipe;
import com.moodfood.domain.RecipeNutrition;
import com.moodfood.repository.RecipeRepository;
import com.moodfood.web.dto.RecipeDetailDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

  private final RecipeRepository recipeRepository;

  public RecipeController(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  @Transactional(readOnly = true)
  @GetMapping("/{id}")
  public RecipeDetailDto get(@PathVariable Long id) {
    Recipe recipe = recipeRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다."));
    return toDto(recipe);
  }

  private RecipeDetailDto toDto(Recipe recipe) {
    RecipeNutrition n = recipe.getNutrition();
    return new RecipeDetailDto(
        recipe.getId(), recipe.getTitle(), recipe.getDescription(), recipe.getServings(),
        recipe.getPrepTimeMin(), recipe.getCookTimeMin(), recipe.getTotalTimeMin(),
        recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null,
        recipe.getCategory(), recipe.getCuisine(), recipe.getImageUrl(),
        recipe.getIngredients().stream()
            .map(i -> new RecipeDetailDto.IngredientDto(i.getRawText(),
                i.getIngredient() != null ? i.getIngredient().getDisplayName() : null))
            .toList(),
        recipe.getSteps().stream().map(s -> s.getInstruction()).toList(),
        n == null ? null : new RecipeDetailDto.NutritionDto(
            n.getCalories(), n.getProteinG(), n.getFatG(), n.getCarbsG(), n.getSodiumMg(), n.getSource()),
        recipe.getTags().stream().map(t -> t.getTag()).toList(),
        new RecipeDetailDto.ProvenanceDto(recipe.getSourceType().name(), recipe.getSourceName(),
            recipe.getSourceUrl(), recipe.getLicense(), recipe.isAttributionRequired())
    );
  }
}
