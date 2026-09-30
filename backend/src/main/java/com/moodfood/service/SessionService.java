package com.moodfood.service;

import com.moodfood.domain.Session;
import com.moodfood.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** No accounts (spec §25/§32.5) — a session is just a client-generated UUID
 * the frontend keeps in localStorage and sends on every request. */
@Service
public class SessionService {

  private final SessionRepository sessionRepository;

  public SessionService(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  @Transactional
  public Session ensure(UUID sessionId) {
    sessionRepository.upsert(sessionId);
    return sessionRepository.findById(sessionId).orElseThrow();
  }
}
