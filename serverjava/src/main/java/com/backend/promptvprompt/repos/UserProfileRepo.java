package com.backend.promptvprompt.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.promptvprompt.models.UserProfile;

@Repository
public interface UserProfileRepo extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByDisplayName(String displayName);

    boolean existsByDisplayName(String displayName);

    Optional<UserProfile> findByUserId(String userId);
}
