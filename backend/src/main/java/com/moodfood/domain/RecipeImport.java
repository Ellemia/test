package com.moodfood.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

/** Raw staging row for one fetched/submitted recipe payload (spec §7).
 * Never written directly into {@link Recipe} — the normalizer reads this,
 * and only an approved, normalized result gets linked via recipeId. */
@Entity
@Table(name = "recipe_imports")
public class RecipeImport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 30)
  private SourceType sourceType;

  @Column(name = "source_name", nullable = false, length = 100)
  private String sourceName;

  @Column(name = "source_url", length = 500)
  private String sourceUrl;

  @Column(name = "external_recipe_id", length = 100)
  private String externalRecipeId;

  @Column(length = 100)
  private String license;

  @Column(name = "attribution_required", nullable = false)
  private boolean attributionRequired;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_payload", nullable = false, columnDefinition = "jsonb")
  private String rawPayload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ImportStatus status = ImportStatus.PENDING;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  @Column(name = "recipe_id")
  private Long recipeId;

  @Column(name = "retrieved_at", nullable = false)
  private Instant retrievedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  protected RecipeImport() {}

  public RecipeImport(SourceType sourceType, String sourceName, String sourceUrl,
                       String externalRecipeId, String license, boolean attributionRequired,
                       String rawPayload, Instant retrievedAt) {
    this.sourceType = sourceType;
    this.sourceName = sourceName;
    this.sourceUrl = sourceUrl;
    this.externalRecipeId = externalRecipeId;
    this.license = license;
    this.attributionRequired = attributionRequired;
    this.rawPayload = rawPayload;
    this.retrievedAt = retrievedAt;
  }

  public Long getId() { return id; }
  public SourceType getSourceType() { return sourceType; }
  public String getSourceName() { return sourceName; }
  public String getSourceUrl() { return sourceUrl; }
  public String getExternalRecipeId() { return externalRecipeId; }
  public String getLicense() { return license; }
  public boolean isAttributionRequired() { return attributionRequired; }
  public String getRawPayload() { return rawPayload; }
  public ImportStatus getStatus() { return status; }
  public String getErrorMessage() { return errorMessage; }
  public Long getRecipeId() { return recipeId; }
  public Instant getRetrievedAt() { return retrievedAt; }

  public void markNormalized(Long recipeId) {
    this.status = ImportStatus.NORMALIZED;
    this.recipeId = recipeId;
    this.updatedAt = Instant.now();
  }

  public void markDuplicate(Long existingRecipeId) {
    this.status = ImportStatus.DUPLICATE;
    this.recipeId = existingRecipeId;
    this.updatedAt = Instant.now();
  }

  public void markError(String message) {
    this.status = ImportStatus.ERROR;
    this.errorMessage = message;
    this.updatedAt = Instant.now();
  }

  public void markReview(String reason) {
    this.status = ImportStatus.REVIEW;
    this.errorMessage = reason;
    this.updatedAt = Instant.now();
  }

  public void markApproved() {
    this.status = ImportStatus.APPROVED;
    this.updatedAt = Instant.now();
  }

  public void markRejected(String reason) {
    this.status = ImportStatus.REJECTED;
    this.errorMessage = reason;
    this.updatedAt = Instant.now();
  }
}
