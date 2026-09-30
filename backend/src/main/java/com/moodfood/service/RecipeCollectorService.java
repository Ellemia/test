package com.moodfood.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodfood.collector.FetchedRecipe;
import com.moodfood.collector.NormalizedRecipe;
import com.moodfood.collector.RawRecipeParser;
import com.moodfood.collector.RecipeSourceAdapter;
import com.moodfood.domain.RecipeImport;
import com.moodfood.domain.SourceType;
import com.moodfood.repository.RecipeImportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates one collector run (spec §6): fetch via an allow-listed
 * adapter, stage each payload as a {@code recipe_imports} row, then run it
 * through {@link RecipeIngestionService}. Every step's outcome (imported,
 * duplicate, needs review, or errored) is recorded on the import row rather
 * than silently discarded.
 */
@Service
public class RecipeCollectorService {

  private final RecipeImportRepository recipeImportRepository;
  private final RecipeIngestionService ingestionService;
  private final Map<String, RecipeSourceAdapter> adaptersByName;
  private final Map<String, RawRecipeParser> parsersByName;
  private final ObjectMapper objectMapper;

  public RecipeCollectorService(RecipeImportRepository recipeImportRepository,
                                 RecipeIngestionService ingestionService,
                                 List<RecipeSourceAdapter> adapters,
                                 List<RawRecipeParser> parsers,
                                 ObjectMapper objectMapper) {
    this.recipeImportRepository = recipeImportRepository;
    this.ingestionService = ingestionService;
    this.adaptersByName = adapters.stream().collect(Collectors.toMap(RecipeSourceAdapter::sourceName, Function.identity()));
    this.parsersByName = parsers.stream().collect(Collectors.toMap(RawRecipeParser::sourceName, Function.identity()));
    this.objectMapper = objectMapper;
  }

  public record RunSummary(int fetched, int created, int duplicate, int review, int error) {}

  /**
   * Direct-entry path for MANUAL/OWN_RECIPE sources (spec §6) — no adapter or
   * external fetch involved, but it still goes through the same staging +
   * dedupe + tagging pipeline as everything else, and still carries full
   * provenance (source_type=OWN_RECIPE is a first-class source, not a
   * placeholder — spec §2.2 priority list, item 4).
   */
  @Transactional
  public RecipeIngestionService.IngestResult ingestOwn(NormalizedRecipe normalized, String sourceName,
                                                         String authorNote) {
    String rawPayload;
    try {
      rawPayload = objectMapper.writeValueAsString(normalized);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize own-recipe payload", e);
    }

    RecipeImport recipeImport = new RecipeImport(
        SourceType.OWN_RECIPE, sourceName, null, null,
        "CC-BY-SA 4.0 (자체 작성, " + authorNote + ")", true, rawPayload, Instant.now());
    recipeImport = recipeImportRepository.save(recipeImport);

    try {
      return ingestionService.ingest(recipeImport, normalized);
    } catch (Exception e) {
      recipeImport.markError(e.getMessage());
      throw e;
    }
  }

  @Transactional
  public RunSummary runImport(String sourceName, String query, int limit) {
    RecipeSourceAdapter adapter = adaptersByName.get(sourceName);
    if (adapter == null) {
      throw new IllegalArgumentException("No adapter registered for source: " + sourceName
          + " (allowed: " + adaptersByName.keySet() + ")");
    }

    List<FetchedRecipe> fetched = adapter.fetch(query, limit);
    int created = 0, duplicate = 0, review = 0, error = 0;

    for (FetchedRecipe f : fetched) {
      RecipeImport recipeImport = new RecipeImport(
          adapter.sourceType(), adapter.sourceName(), f.sourceUrl(), f.externalId(),
          adapter.license(), adapter.attributionRequired(), f.rawPayloadJson(), Instant.now());
      recipeImport = recipeImportRepository.save(recipeImport);

      try {
        NormalizedRecipe normalized = normalize(sourceName, f.rawPayloadJson());
        var result = ingestionService.ingest(recipeImport, normalized);
        if (result instanceof RecipeIngestionService.IngestResult.Created) created++;
        else if (result instanceof RecipeIngestionService.IngestResult.Duplicate) duplicate++;
        else review++;
      } catch (Exception e) {
        recipeImport.markError(e.getMessage());
        error++;
      }
    }

    return new RunSummary(fetched.size(), created, duplicate, review, error);
  }

  private NormalizedRecipe normalize(String sourceName, String rawPayloadJson) {
    RawRecipeParser parser = parsersByName.get(sourceName);
    if (parser == null) {
      throw new IllegalStateException("No parser registered for source: " + sourceName);
    }
    return parser.parse(rawPayloadJson);
  }
}
