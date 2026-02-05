package com.learnsmart.content.client;

import com.learnsmart.content.dto.AiDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", path = "/v1")
public interface AiServiceClient {

    @PostMapping("/contents/lessons")
    AiDtos.GenerateLessonsResponse generateLessons(@RequestBody AiDtos.GenerateLessonsRequest request);
}
