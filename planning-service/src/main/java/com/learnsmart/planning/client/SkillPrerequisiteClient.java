package com.learnsmart.planning.client;

import com.learnsmart.planning.dto.PrerequisiteDtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Client for fetching skill prerequisites from content-service.
 * US-111: Skill Prerequisite Validation in Planning.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SkillPrerequisiteClient {

    private final RestTemplate restTemplate;

    @Value("${content-service.url:http://content-service:8080}")
    private String contentServiceUrl;

    /**
     * Fetches prerequisites for a given skill.
     * GET /skills/{skillId}/prerequisites
     *
     * @param skillId the skill ID
     * @return list of prerequisite skill DTOs
     */
    public List<PrerequisiteDtos.SkillDto> getPrerequisites(UUID skillId) {
        try {
            String url = contentServiceUrl + "/skills/" + skillId + "/prerequisites";
            log.debug("Fetching prerequisites for skill: {}", skillId);

            ResponseEntity<List<PrerequisiteDtos.SkillDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PrerequisiteDtos.SkillDto>>() {
                    });

            List<PrerequisiteDtos.SkillDto> prerequisites = response.getBody();
            log.debug("Found {} prerequisites for skill {}",
                    prerequisites != null ? prerequisites.size() : 0, skillId);

            return prerequisites != null ? prerequisites : Collections.emptyList();

        } catch (Exception e) {
            log.warn("Failed to fetch prerequisites for skill {}: {}", skillId, e.getMessage());
            return Collections.emptyList(); // Graceful degradation
        }
    }

    /**
     * Fetches skill graph for multiple skills.
     * Returns map of skillId -> list of prerequisite skill IDs.
     *
     * @param skillIds list of skill IDs
     * @return map of skill ID to prerequisite IDs
     */
    public Map<UUID, List<UUID>> getSkillGraph(List<UUID> skillIds) {
        Map<UUID, List<UUID>> skillGraph = new HashMap<>();

        for (UUID skillId : skillIds) {
            List<PrerequisiteDtos.SkillDto> prerequisites = getPrerequisites(skillId);
            List<UUID> prerequisiteIds = prerequisites.stream()
                    .map(PrerequisiteDtos.SkillDto::getId)
                    .toList();
            skillGraph.put(skillId, prerequisiteIds);
        }

        log.info("Built skill graph for {} skills with {} total prerequisites",
                skillIds.size(),
                skillGraph.values().stream().mapToInt(List::size).sum());

        return skillGraph;
    }
}
