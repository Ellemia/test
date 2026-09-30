package com.moodfood.domain;

/** Data source priority per spec §2.2 — public API/open dataset outrank scraping,
 * which this project never does; OWN_RECIPE is a first-class legitimate source,
 * not a placeholder. */
public enum SourceType {
  PUBLIC_API,
  OPEN_DATASET,
  MANUAL,
  OWN_RECIPE
}
