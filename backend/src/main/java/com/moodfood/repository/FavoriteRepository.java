package com.moodfood.repository;

import com.moodfood.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  List<Favorite> findBySessionId(UUID sessionId);
  Optional<Favorite> findBySessionIdAndRecipeId(UUID sessionId, Long recipeId);
  void deleteBySessionIdAndRecipeId(UUID sessionId, Long recipeId);
}
