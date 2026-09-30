package com.moodfood.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

/** Accumulated personalization vector per session (spec §17), stored as a
 * JSON map of dimension -> score so new score dimensions don't need a migration. */
@Entity
@Table(name = "user_preferences")
public class UserPreference {
  @Id
  @Column(name = "session_id")
  private UUID sessionId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private String preference;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  protected UserPreference() {}

  public UserPreference(UUID sessionId, String preference) {
    this.sessionId = sessionId;
    this.preference = preference;
  }

  public UUID getSessionId() { return sessionId; }
  public String getPreference() { return preference; }
  public void setPreference(String preference) {
    this.preference = preference;
    this.updatedAt = Instant.now();
  }
}
