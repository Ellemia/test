package com.moodfood.collector.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodfood.collector.NormalizedRecipe;
import com.moodfood.collector.NormalizedRecipe.NormalizedIngredient;
import com.moodfood.collector.RawRecipeParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Parses one TheMealDB {@code lookup.php} meal object (as stored verbatim in
 * {@code recipe_imports.raw_payload}) into the canonical shape.
 *
 * TheMealDB has no explicit prep/cook-time or difficulty field, and its
 * strMeasureN values are free text ("3/4 cup", "200g") rather than a
 * quantity+unit pair — quantity/unit extraction from that text is left for a
 * later ingredient-normalization pass (spec §9); for now the raw "amount
 * ingredient" text is kept as-is in {@code rawText} so nothing is lost. */
@Component
public class TheMealDbParser implements RawRecipeParser {

  private final ObjectMapper objectMapper;

  public TheMealDbParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String sourceName() { return "TheMealDB"; }

  @Override
  public NormalizedRecipe parse(String rawPayloadJson) {
    JsonNode meal;
    try {
      meal = objectMapper.readTree(rawPayloadJson);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid TheMealDB payload JSON", e);
    }

    String title = textOrNull(meal, "strMeal");
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("TheMealDB payload missing strMeal");
    }

    List<NormalizedIngredient> ingredients = new ArrayList<>();
    for (int i = 1; i <= 20; i++) {
      String ingredient = textOrNull(meal, "strIngredient" + i);
      String measure = textOrNull(meal, "strMeasure" + i);
      if (ingredient == null || ingredient.isBlank()) continue;
      String rawText = (measure == null || measure.isBlank())
          ? ingredient.trim()
          : (measure.trim() + " " + ingredient.trim());
      ingredients.add(new NormalizedIngredient(rawText, null, null));
    }

    List<String> steps = splitInstructions(textOrNull(meal, "strInstructions"));

    List<String> tags = new ArrayList<>();
    String tagsRaw = textOrNull(meal, "strTags");
    if (tagsRaw != null && !tagsRaw.isBlank()) {
      Arrays.stream(tagsRaw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(tags::add);
    }

    return new NormalizedRecipe(
        title,
        null,               // no description field on TheMealDB
        4,                  // TheMealDB doesn't publish servings; a reasonable family-size default
        null,
        null,
        null,
        null,               // difficulty unknown from source — left for the tagger/admin to set
        textOrNull(meal, "strCategory"),
        textOrNull(meal, "strArea"),
        textOrNull(meal, "strMealThumb"),
        ingredients,
        steps,
        tags
    );
  }

  private static String textOrNull(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return (v == null || v.isNull()) ? null : v.asText();
  }

  private static List<String> splitInstructions(String instructions) {
    if (instructions == null || instructions.isBlank()) return List.of();
    String[] byNewline = instructions.strip().split("\\r?\\n+");
    List<String> steps = new ArrayList<>();
    for (String line : byNewline) {
      String trimmed = line.strip().replaceFirst("^\\d+[.)]\\s*", "");
      if (!trimmed.isBlank()) steps.add(trimmed);
    }
    return steps.isEmpty() ? List.of(instructions.strip()) : steps;
  }
}
