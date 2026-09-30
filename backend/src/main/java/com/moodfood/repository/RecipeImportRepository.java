package com.moodfood.repository;

import com.moodfood.domain.ImportStatus;
import com.moodfood.domain.RecipeImport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeImportRepository extends JpaRepository<RecipeImport, Long> {
  List<RecipeImport> findByStatus(ImportStatus status);
}
