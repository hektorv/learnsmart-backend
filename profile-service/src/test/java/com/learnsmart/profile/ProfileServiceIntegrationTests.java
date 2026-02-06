package com.learnsmart.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnsmart.profile.dto.ProfileDtos.UserGoalCreateRequest;
import com.learnsmart.profile.dto.ProfileDtos.UserRegistrationRequest;
import com.learnsmart.profile.dto.ProfileDtos.UserProfileResponse;
import com.learnsmart.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "eureka.client.enabled=false" // Disable Eureka for tests
})
@AutoConfigureMockMvc
class ProfileServiceIntegrationTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserProfileRepository profileRepository;

        @BeforeEach
        void setup() {
                profileRepository.deleteAll();
        }

        @Test
        void testFullFlow_RegisterAndManageGoals() throws Exception {
                // 1. Register User
                UserRegistrationRequest registerRequest = UserRegistrationRequest.builder()
                                .email("test@unir.net")
                                .password("password123")
                                .displayName("Hector Student")
                                .locale("es-ES")
                                .build();

                MvcResult result = mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email").value("test@unir.net"))
                                .andExpect(jsonPath("$.userId").exists())
                                .andReturn();

                UserProfileResponse profile = objectMapper.readValue(
                                result.getResponse().getContentAsString(), UserProfileResponse.class);

                UUID userId = profile.getUserId();
                String userIdStr = userId.toString();

                // 2. Get Profile Me
                mockMvc.perform(get("/profiles/me")
                                .header("X-User-Id", userIdStr))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.displayName").value("Hector Student"));

                // 3. Create Goal
                UserGoalCreateRequest goalRequest = UserGoalCreateRequest.builder()
                                .title("Learn Spring Boot")
                                .description("Master Microservices")
                                .domainId(UUID.randomUUID())
                                .intensity("standard")
                                .dueDate(LocalDate.now().plusMonths(3))
                                .build();

                mockMvc.perform(post("/profiles/me/goals")
                                .header("X-User-Id", userIdStr)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(goalRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value("Learn Spring Boot"));

                // 4. List Goals
                mockMvc.perform(get("/profiles/me/goals")
                                .header("X-User-Id", userIdStr))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].title").value("Learn Spring Boot"));
        }
}
