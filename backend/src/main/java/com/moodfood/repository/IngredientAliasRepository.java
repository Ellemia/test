package com.moodfood.repository;

import com.moodfood.domain.IngredientAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientAliasRepository extends JpaRepository<IngredientAlias, Long> {
  Optional<IngredientAlias> findByAlias(String alias);
}
