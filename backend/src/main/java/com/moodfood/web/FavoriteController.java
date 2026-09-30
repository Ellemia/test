package com.moodfood.web;

import com.moodfood.domain.Favorite;
import com.moodfood.repository.FavoriteRepository;
import com.moodfood.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

  private final FavoriteRepository favoriteRepository;
  private final SessionService sessionService;

  public FavoriteController(FavoriteRepository favoriteRepository, SessionService sessionService) {
    this.favoriteRepository = favoriteRepository;
    this.sessionService = sessionService;
  }

  public record FavoriteDto(Long recipeId) {}

  @GetMapping
  public List<Long> list(@RequestHeader("X-Session-Id") UUID sessionId) {
    return favoriteRepository.findBySessionId(sessionId).stream().map(Favorite::getRecipeId).toList();
  }

  @PostMapping("/{recipeId}")
  public ResponseEntity<Void> add(@RequestHeader("X-Session-Id") UUID sessionId, @PathVariable Long recipeId) {
    sessionService.ensure(sessionId);
    if (favoriteRepository.findBySessionIdAndRecipeId(sessionId, recipeId).isEmpty()) {
      favoriteRepository.save(new Favorite(sessionId, recipeId));
    }
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{recipeId}")
  public ResponseEntity<Void> remove(@RequestHeader("X-Session-Id") UUID sessionId, @PathVariable Long recipeId) {
    favoriteRepository.deleteBySessionIdAndRecipeId(sessionId, recipeId);
    return ResponseEntity.noContent().build();
  }
}
