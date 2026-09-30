package com.moodfood.service;

import com.moodfood.domain.Recipe;
import com.moodfood.domain.SourceType;
import com.moodfood.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Duplicate detection (spec §10), tiered:
 *   1) same source + external id — exact re-import of the same source record
 *   2) normalized title match — different source, same dish name
 * Tier 3 (title/ingredient/step similarity scoring) is not implemented yet;
 * a title match here is flagged for REVIEW rather than auto-merged or
 * auto-rejected, matching the spec's "관리자 승인 방식으로 처리" rule.
 */
@Service
public class DeduplicationService {

  private final RecipeRepository recipeRepository;

  public DeduplicationService(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  public Optional<Recipe> findExactSourceMatch(SourceType sourceType, String sourceName, String sourceRecipeId) {
    if (sourceRecipeId == null) return Optional.empty();
    return recipeRepository.findBySourceTypeAndSourceNameAndSourceRecipeId(sourceType, sourceName, sourceRecipeId);
  }

  public List<Recipe> findTitleCandidates(String title) {
    return recipeRepository.findByNormalizedTitle(title.trim());
  }
}
