package com.learnsmart.planning.dto;

import lombok.Data;
import lombok.Builder;
import java.util.UUID;
import java.util.List;

/**
 * DTOs for prerequisite validation (US-111).
 */
public class PrerequisiteDtos {

    /**
     * Represents a prerequisite violation in a learning plan.
     */
    @Data
    @Builder
    public static class PrerequisiteViolation {
        private String skillId;
        private String skillName;
        private int moduleIndex;
        private String prerequisiteSkillId;
        private String prerequisiteSkillName;
        private Integer prerequisiteModuleIndex; // null if not in plan
        private String message;
    }

    /**
     * Skill DTO from content-service.
     */
    @Data
    public static class SkillDto {
        private UUID id;
        private String code;
        private String name;
        private String description;
        private String level;
        private List<UUID> prerequisiteIds; // IDs of prerequisite skills
    }
}
