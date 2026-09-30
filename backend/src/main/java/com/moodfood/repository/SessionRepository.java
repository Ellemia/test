package com.moodfood.repository;

import com.moodfood.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

  /** Atomic upsert so two concurrent first-requests for the same client-generated
   * session id (e.g. React StrictMode's double effect, or a double-click) never
   * race on a plain findById-then-insert and hit the sessions_pkey constraint. */
  @Modifying
  @Query(value = "INSERT INTO sessions (id, created_at, last_seen_at) VALUES (:id, now(), now()) "
      + "ON CONFLICT (id) DO UPDATE SET last_seen_at = now()", nativeQuery = true)
  void upsert(@Param("id") UUID id);
}
