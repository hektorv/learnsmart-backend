package com.learnsmart.assessment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

@FeignClient(name = "planning-service")
public interface PlanningClient {

    @PostMapping("/plans/{id}/replan")
    Object replan(@PathVariable("id") UUID id, @RequestParam("reason") String reason, @RequestBody String constraints);
}
