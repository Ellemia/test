package com.moodfood.collector;

/** Converts one source's raw JSON payload into the canonical shape. One
 * implementation per source — the Normalizer service dispatches by source name. */
public interface RawRecipeParser {
  String sourceName();
  NormalizedRecipe parse(String rawPayloadJson);
}
