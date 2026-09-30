package com.moodfood.collector;

import com.moodfood.domain.SourceType;

import java.util.List;

/** One allowed external source (spec §6). Only adapters that exist here may be
 * used to import data — an operator can never point the collector at an
 * arbitrary URL and scrape it. */
public interface RecipeSourceAdapter {
  String sourceName();
  SourceType sourceType();
  String license();
  boolean attributionRequired();

  /**
   * Fetches up to {@code limit} raw recipe payloads matching {@code query}
   * (adapter-specific meaning — e.g. a category name) and returns them
   * unmodified, ready for {@code recipe_imports} staging.
   */
  List<FetchedRecipe> fetch(String query, int limit);
}
