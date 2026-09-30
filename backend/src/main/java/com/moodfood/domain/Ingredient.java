package com.moodfood.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ingredients")
public class Ingredient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "canonical_name", nullable = false, unique = true, length = 100)
  private String canonicalName;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(length = 50)
  private String category;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected Ingredient() {}

  public Ingredient(String canonicalName, String displayName, String category) {
    this.canonicalName = canonicalName;
    this.displayName = displayName;
    this.category = category;
  }

  public Long getId() { return id; }
  public String getCanonicalName() { return canonicalName; }
  public String getDisplayName() { return displayName; }
  public String getCategory() { return category; }
}
