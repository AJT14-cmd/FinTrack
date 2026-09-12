package com.ajthapa.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentDescribesAuthenticationAndErrorResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("FinTrack API"))
                .andExpect(jsonPath("$.info.version").value("1.0"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/users/me'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/users/me'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/users/me'].get.responses['404']").exists());
    }

    @Test
    void swaggerUiIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
