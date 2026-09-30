package com.moodfood.repository;

import com.moodfood.domain.QualityStatus;
import com.moodfood.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
  List<Recipe> findByQualityStatus(QualityStatus status);

  @Query("select r from Recipe r where r.qualityStatus = :status")
  List<Recipe> findPublished(@Param("status") QualityStatus status);

  Optional<Recipe> findBySourceTypeAndSourceNameAndSourceRecipeId(
      com.moodfood.domain.SourceType sourceType, String sourceName, String sourceRecipeId);

  @Query("select r from Recipe r where lower(r.title) = lower(:title)")
  List<Recipe> findByNormalizedTitle(@Param("title") String title);
}
