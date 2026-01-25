package com.backend.promptvprompt.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.backend.promptvprompt.models.ScenarioTemplate;

public interface ScenarioTemplateRepo extends JpaRepository<ScenarioTemplate, String> {
    @Query(value = "SELECT * FROM scenario_template OFFSET :offset LIMIT 1", nativeQuery = true)
    Optional<ScenarioTemplate> findRandomTemplate(int offset);
}
