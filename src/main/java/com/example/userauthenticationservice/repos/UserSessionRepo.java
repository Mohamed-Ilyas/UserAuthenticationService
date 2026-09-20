package com.example.userauthenticationservice.repos;

import com.example.userauthenticationservice.models.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepo extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByToken(String token);
}
