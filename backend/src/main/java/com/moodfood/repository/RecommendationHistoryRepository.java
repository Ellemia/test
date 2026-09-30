package com.moodfood.repository;

import com.moodfood.domain.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
  List<RecommendationHistory> findBySessionIdAndShownAtAfter(UUID sessionId, Instant after);
}
