package com.moodfood.service;

import com.moodfood.domain.Ingredient;
import com.moodfood.domain.IngredientAlias;
import com.moodfood.repository.IngredientAliasRepository;
import com.moodfood.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Maps free-text ingredient mentions ("돼지고기 앞다리살 200그램", "pork shoulder")
 * to one canonical {@link Ingredient} (spec §9).
 *
 * This is exact-alias-match only, not fuzzy/semantic matching — a real
 * "돼지고기"/"돼지 앞다리"/"pork shoulder" merge needs a curated alias table
 * (or an LLM pass per spec §26/§27) that this MVP doesn't build yet. Unknown
 * text becomes its own new canonical ingredient rather than silently
 * dropping data, so nothing collected is lost while the alias table grows.
 */
@Service
public class IngredientNormalizationService {

  private static final Pattern LEADING_QUANTITY =
      Pattern.compile("^[\\d./½¼¾⅓⅔\\s]+([a-zA-Z가-힣]{0,4}\\.?)?\\s+", Pattern.UNICODE_CASE);

  private final IngredientRepository ingredientRepository;
  private final IngredientAliasRepository aliasRepository;

  public IngredientNormalizationService(IngredientRepository ingredientRepository,
                                         IngredientAliasRepository aliasRepository) {
    this.ingredientRepository = ingredientRepository;
    this.aliasRepository = aliasRepository;
  }

  @Transactional
  public Ingredient resolve(String rawIngredientText) {
    String guess = stripLeadingQuantity(rawIngredientText);
    String normalized = normalize(guess);

    return aliasRepository.findByAlias(normalized)
        .map(IngredientAlias::getIngredient)
        .orElseGet(() -> ingredientRepository.findByCanonicalName(normalized)
            .orElseGet(() -> createIngredient(normalized, guess)));
  }

  private Ingredient createIngredient(String canonicalName, String displayName) {
    Ingredient ingredient = new Ingredient(canonicalName, displayName, null);
    return ingredientRepository.save(ingredient);
  }

  /** Registers an additional surface form for an existing canonical ingredient. */
  @Transactional
  public void addAlias(Ingredient ingredient, String alias) {
    String normalized = normalize(alias);
    if (aliasRepository.findByAlias(normalized).isPresent()) return;
    aliasRepository.save(new IngredientAlias(ingredient, normalized));
  }

  private static String normalize(String text) {
    return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }

  private static String stripLeadingQuantity(String rawText) {
    return LEADING_QUANTITY.matcher(rawText.trim()).replaceFirst("").trim();
  }
}
