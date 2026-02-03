package com.learnsmart.assessment.client;

import com.learnsmart.assessment.dto.AiDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service")
public interface AiClient {

    @PostMapping("/v1/assessments/items")
    AiDtos.NextItemResponse getNextItem(@RequestBody AiDtos.NextItemRequest request);

    @PostMapping("/v1/assessments/feedback")
    AiDtos.FeedbackResponse getFeedback(@RequestBody AiDtos.FeedbackRequest request);
}
