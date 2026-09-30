package com.moodfood.collector.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodfood.collector.FetchedRecipe;
import com.moodfood.collector.RecipeSourceAdapter;
import com.moodfood.domain.SourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * TheMealDB (spec §5.2/§6) — free tier, no signup, test key "1". Terms (see
 * themealdb.com/terms_of_use.php): free for dev/non-commercial use, data may
 * be stored/modified via the official endpoints, attribution required,
 * reselling the API itself is forbidden. Commercial/app-store distribution
 * needs their paid tier — out of scope for this project's current stage.
 *
 * {@code query} here is a TheMealDB category name (e.g. "Seafood", "Dessert",
 * "Vegetarian") — see {@code filter.php?c=}.
 */
@Component
public class TheMealDbAdapter implements RecipeSourceAdapter {

  private static final String LICENSE =
      "TheMealDB Terms of Use (free tier, non-commercial, attribution required)";

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  public TheMealDbAdapter(RestClient.Builder builder, ObjectMapper objectMapper,
                           @Value("${moodfood.collector.themealdb.base-url}") String baseUrl) {
    this.baseUrl = baseUrl;
    this.restClient = builder.baseUrl(baseUrl).build();
    this.objectMapper = objectMapper;
  }

  @Override
  public String sourceName() { return "TheMealDB"; }

  @Override
  public SourceType sourceType() { return SourceType.PUBLIC_API; }

  @Override
  public String license() { return LICENSE; }

  @Override
  public boolean attributionRequired() { return true; }

  @Override
  public List<FetchedRecipe> fetch(String category, int limit) {
    JsonNode filterResult = restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/filter.php").queryParam("c", category).build())
        .retrieve()
        .body(JsonNode.class);

    List<FetchedRecipe> results = new ArrayList<>();
    if (filterResult == null || !filterResult.has("meals") || filterResult.get("meals").isNull()) {
      return results;
    }

    JsonNode meals = filterResult.get("meals");
    int count = 0;
    for (JsonNode summary : meals) {
      if (count >= limit) break;
      String id = summary.path("idMeal").asText(null);
      if (id == null) continue;

      FetchedRecipe full = lookupById(id);
      if (full != null) {
        results.add(full);
        count++;
      }
    }
    return results;
  }

  /** {@code lookup.php?i=} — full recipe detail for one meal id. */
  public FetchedRecipe lookupById(String mealId) {
    JsonNode lookupResult = restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/lookup.php").queryParam("i", mealId).build())
        .retrieve()
        .body(JsonNode.class);

    if (lookupResult == null || !lookupResult.has("meals") || lookupResult.get("meals").isNull()
        || lookupResult.get("meals").isEmpty()) {
      return null;
    }

    JsonNode meal = lookupResult.get("meals").get(0);
    String rawJson;
    try {
      rawJson = objectMapper.writeValueAsString(meal);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize TheMealDB payload for meal " + mealId, e);
    }

    String sourceUrl = baseUrl.replace("/api/json/v1/1", "") + "/meal/" + mealId;
    return new FetchedRecipe(mealId, sourceUrl, rawJson);
  }
}
