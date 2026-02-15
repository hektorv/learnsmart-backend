package com.learnsmart.content.client;

import com.learnsmart.content.dto.AiDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", path = "/v1")
public interface AiServiceClient {

        @PostMapping("/contents/lessons")
        AiDtos.GenerateLessonsResponse generateLessons(@RequestBody AiDtos.GenerateLessonsRequest request);

        @PostMapping("/contents/skills")
        AiDtos.GenerateSkillsResponse generateSkills(@RequestBody AiDtos.GenerateSkillsRequest request);

        @PostMapping("/contents/skills/prerequisites")
        AiDtos.GeneratePrerequisitesResponse generatePrerequisites(
                        @RequestBody AiDtos.GeneratePrerequisitesRequest request);

        // US-10-08: AI Assessment Item Generation
        @PostMapping("/contents/assessment-items")
        AiDtos.GenerateAssessmentItemsResponse generateAssessmentItems(
                        @RequestBody AiDtos.GenerateAssessmentItemsRequest request);

        // US-10-09: AI Skill Tagging
        @PostMapping("/contents/skill-tags")
        AiDtos.AnalyzeSkillTagsResponse analyzeSkillTags(
                        @RequestBody AiDtos.AnalyzeSkillTagsRequest request);
}
