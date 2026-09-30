package com.moodfood.collector;

/** One raw payload fetched from an external source, ready to be staged into
 * recipe_imports (spec §7) — not yet normalized or validated. */
public record FetchedRecipe(String externalId, String sourceUrl, String rawPayloadJson) {
}
