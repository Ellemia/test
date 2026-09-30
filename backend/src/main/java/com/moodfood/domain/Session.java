package com.moodfood.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {
  @Id
  private UUID id;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "last_seen_at", nullable = false)
  private Instant lastSeenAt;

  protected Session() {}

  public Session(UUID id) {
    this.id = id;
    this.createdAt = Instant.now();
    this.lastSeenAt = this.createdAt;
  }

  public UUID getId() { return id; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getLastSeenAt() { return lastSeenAt; }
  public void touch() { this.lastSeenAt = Instant.now(); }
}
