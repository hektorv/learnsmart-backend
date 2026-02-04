package com.learnsmart.planning.service;

import com.learnsmart.planning.dto.PrerequisiteDtos;
import com.learnsmart.planning.model.LearningPlan;
import com.learnsmart.planning.model.PlanModule;
import com.learnsmart.planning.model.PlanActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for validating and enforcing skill prerequisite ordering in learning
 * plans.
 * US-111: Skill Prerequisite Validation in Planning.
 */
@Service
@Slf4j
public class PrerequisiteValidationService {

    /**
     * Validates that all prerequisites appear before dependent skills in the plan.
     *
     * @param plan       the learning plan to validate
     * @param skillGraph map of skill ID to list of prerequisite IDs
     * @return list of violations found (empty if valid)
     */
    public List<PrerequisiteDtos.PrerequisiteViolation> validatePlan(
            LearningPlan plan,
            Map<UUID, List<UUID>> skillGraph) {

        List<PrerequisiteDtos.PrerequisiteViolation> violations = new ArrayList<>();

        if (plan.getModules() == null || plan.getModules().isEmpty()) {
            log.debug("Plan has no modules, skipping prerequisite validation");
            return violations;
        }

        // Build skill → module index map
        Map<UUID, Integer> skillToModuleIndex = buildSkillToModuleMap(plan);

        // Validate each skill's prerequisites
        for (Map.Entry<UUID, Integer> entry : skillToModuleIndex.entrySet()) {
            UUID skillId = entry.getKey();
            int moduleIndex = entry.getValue();

            List<UUID> prerequisites = skillGraph.getOrDefault(skillId, Collections.emptyList());

            for (UUID prerequisiteId : prerequisites) {
                Integer prerequisiteIndex = skillToModuleIndex.get(prerequisiteId);

                // Check if prerequisite appears after the skill (violation)
                if (prerequisiteIndex != null && prerequisiteIndex >= moduleIndex) {
                    PrerequisiteDtos.PrerequisiteViolation violation = PrerequisiteDtos.PrerequisiteViolation.builder()
                            .skillId(skillId.toString())
                            .skillName("Skill-" + skillId) // Could be enriched with actual name
                            .moduleIndex(moduleIndex)
                            .prerequisiteSkillId(prerequisiteId.toString())
                            .prerequisiteSkillName("Skill-" + prerequisiteId)
                            .prerequisiteModuleIndex(prerequisiteIndex)
                            .message(String.format(
                                    "Skill %s in module %d requires prerequisite %s which appears in module %d",
                                    skillId, moduleIndex, prerequisiteId, prerequisiteIndex))
                            .build();

                    violations.add(violation);
                    log.warn("Prerequisite violation: {}", violation.getMessage());
                }
                // Note: Missing prerequisites (not in plan) are not violations
                // User may already know them
            }
        }

        log.info("Prerequisite validation complete: {} violations found", violations.size());
        return violations;
    }

    /**
     * Attempts to re-order modules to satisfy prerequisite constraints.
     * Uses topological sort to determine valid ordering.
     *
     * @param plan       the learning plan to reorder
     * @param skillGraph map of skill ID to list of prerequisite IDs
     * @return reordered plan
     * @throws IllegalStateException if cyclic dependencies detected or reordering
     *                               impossible
     */
    public LearningPlan reorderForPrerequisites(
            LearningPlan plan,
            Map<UUID, List<UUID>> skillGraph) {

        if (plan.getModules() == null || plan.getModules().isEmpty()) {
            return plan;
        }

        // Extract all skills from plan
        List<UUID> skillsInPlan = extractSkillIds(plan);

        // Filter skill graph to only include skills in the plan
        Map<UUID, List<UUID>> filteredGraph = new HashMap<>();
        for (UUID skillId : skillsInPlan) {
            List<UUID> prerequisites = skillGraph.getOrDefault(skillId, Collections.emptyList())
                    .stream()
                    .filter(skillsInPlan::contains) // Only prerequisites that are in the plan
                    .toList();
            filteredGraph.put(skillId, prerequisites);
        }

        // Topological sort
        List<UUID> sortedSkills = topologicalSort(filteredGraph);

        // Build skill → sorted index map
        Map<UUID, Integer> skillToSortedIndex = new HashMap<>();
        for (int i = 0; i < sortedSkills.size(); i++) {
            skillToSortedIndex.put(sortedSkills.get(i), i);
        }

        // Reorder modules based on topological sort
        List<PlanModule> reorderedModules = new ArrayList<>(plan.getModules());
        reorderedModules.sort((m1, m2) -> {
            // Get the earliest skill index for each module
            int m1MinIndex = getMinSkillIndex(m1, skillToSortedIndex);
            int m2MinIndex = getMinSkillIndex(m2, skillToSortedIndex);
            return Integer.compare(m1MinIndex, m2MinIndex);
        });

        // Update module positions
        for (int i = 0; i < reorderedModules.size(); i++) {
            reorderedModules.get(i).setPosition(i + 1);
        }

        plan.setModules(reorderedModules);
        log.info("Successfully reordered {} modules to satisfy prerequisites", reorderedModules.size());

        return plan;
    }

    /**
     * Builds a map of skill ID to module index (0-based).
     */
    private Map<UUID, Integer> buildSkillToModuleMap(LearningPlan plan) {
        Map<UUID, Integer> skillToModule = new HashMap<>();

        List<PlanModule> modules = plan.getModules();
        for (int i = 0; i < modules.size(); i++) {
            PlanModule module = modules.get(i);

            // Extract skill IDs from module (simplified - assumes module title contains
            // skill reference)
            // In a real implementation, you'd extract from activities or module metadata
            UUID skillId = extractSkillIdFromModule(module);
            if (skillId != null) {
                skillToModule.put(skillId, i);
            }
        }

        return skillToModule;
    }

    /**
     * Extracts all skill IDs from the plan.
     */
    private List<UUID> extractSkillIds(LearningPlan plan) {
        return plan.getModules().stream()
                .map(this::extractSkillIdFromModule)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * Extracts skill ID from module.
     * Uses the targetSkills field which contains skill codes/IDs.
     */
    private UUID extractSkillIdFromModule(PlanModule module) {
        if (module.getTargetSkills() != null && !module.getTargetSkills().isEmpty()) {
            // Get first target skill
            String skillRef = module.getTargetSkills().get(0);

            // Try to parse as UUID
            try {
                return UUID.fromString(skillRef);
            } catch (IllegalArgumentException e) {
                log.debug("Skill reference '{}' is not a UUID, skipping", skillRef);
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the minimum skill index for a module (earliest prerequisite).
     */
    private int getMinSkillIndex(PlanModule module, Map<UUID, Integer> skillToSortedIndex) {
        UUID skillId = extractSkillIdFromModule(module);
        if (skillId != null && skillToSortedIndex.containsKey(skillId)) {
            return skillToSortedIndex.get(skillId);
        }
        return Integer.MAX_VALUE; // No skill found, put at end
    }

    /**
     * Performs topological sort on skill graph.
     *
     * @param graph map of skill ID to prerequisite IDs
     * @return list of skills in topological order
     * @throws IllegalStateException if cyclic dependency detected
     */
    private List<UUID> topologicalSort(Map<UUID, List<UUID>> graph) {
        List<UUID> sorted = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Set<UUID> visiting = new HashSet<>();

        for (UUID skill : graph.keySet()) {
            if (!visited.contains(skill)) {
                topologicalSortDFS(skill, graph, visited, visiting, sorted);
            }
        }

        Collections.reverse(sorted); // DFS gives reverse topological order
        return sorted;
    }

    /**
     * DFS helper for topological sort.
     */
    private void topologicalSortDFS(
            UUID skill,
            Map<UUID, List<UUID>> graph,
            Set<UUID> visited,
            Set<UUID> visiting,
            List<UUID> sorted) {

        if (visiting.contains(skill)) {
            throw new IllegalStateException("Cyclic dependency detected involving skill: " + skill);
        }

        if (visited.contains(skill)) {
            return;
        }

        visiting.add(skill);

        List<UUID> prerequisites = graph.getOrDefault(skill, Collections.emptyList());
        for (UUID prerequisite : prerequisites) {
            topologicalSortDFS(prerequisite, graph, visited, visiting, sorted);
        }

        visiting.remove(skill);
        visited.add(skill);
        sorted.add(skill);
    }
}
