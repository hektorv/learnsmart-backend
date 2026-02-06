package com.learnsmart.profile.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@FeignClient(name = "content-service")
public interface ContentServiceClient {

    @GetMapping("/domains")
    List<DomainDto> getDomains(@RequestParam("code") String code);

    @GetMapping("/domains/{id}")
    DomainDto getDomain(@PathVariable("id") UUID id);

    @GetMapping("/skills")
    List<SkillDto> getSkills(@RequestParam("code") String code);

    @GetMapping("/skills/{id}")
    SkillDto getSkill(@PathVariable("id") UUID id);

    @Data
    class DomainDto {
        private UUID id;
        private String code;
        private String name;
    }

    @Data
    class SkillDto {
        private UUID id;
        private String code;
        private String name;
        private String level;
        private DomainDto domain;
    }
}
