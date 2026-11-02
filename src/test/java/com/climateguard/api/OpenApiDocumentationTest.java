package com.climateguard.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OpenApiDocumentationTest {

    @Autowired
    private OpenAPI openAPI;

    private PathItem getPath(String path) {
        Map<String, PathItem> paths = openAPI.getPaths();
        assertThat(paths).containsKey(path);
        return paths.get(path);
    }

    @Nested
    @DisplayName("Auth endpoints")
    class AuthEndpoints {

        @Test
        @DisplayName("POST /api/v1/auth/register documents 200, 400, 409")
        void registerHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/auth/register");
            ApiResponses responses = path.getPost().getResponses();

            assertThat(responses).containsKeys("200", "400", "409");
            assertThat(responses.get("200").getDescription()).contains("registered");
            assertThat(responses.get("400").getDescription()).contains("Validation");
            assertThat(responses.get("409").getDescription()).contains("already exists");
        }

        @Test
        @DisplayName("POST /api/v1/auth/register 200 response has example payload")
        void registerSuccessHasExample() {
            PathItem path = getPath("/api/v1/auth/register");
            ApiResponse success = path.getPost().getResponses().get("200");
            Content content = success.getContent();

            assertThat(content).containsKey("application/json");
            MediaType mediaType = content.get("application/json");
            assertThat(mediaType.getExamples()).isNotEmpty();
            String example = mediaType.getExamples().values().iterator().next().getValue().toString();
            assertThat(example).contains("token");
            assertThat(example).contains("username");
        }

        @Test
        @DisplayName("POST /api/v1/auth/login documents 200, 400, 401")
        void loginHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/auth/login");
            ApiResponses responses = path.getPost().getResponses();

            assertThat(responses).containsKeys("200", "400", "401");
            assertThat(responses.get("200").getDescription()).contains("Login successful");
            assertThat(responses.get("401").getDescription()).contains("Invalid credentials");
        }

        @Test
        @DisplayName("POST /api/v1/auth/login 401 response has example payload")
        void loginUnauthorizedHasExample() {
            PathItem path = getPath("/api/v1/auth/login");
            ApiResponse unauthorized = path.getPost().getResponses().get("401");
            Content content = unauthorized.getContent();

            assertThat(content).containsKey("application/json");
            MediaType mediaType = content.get("application/json");
            assertThat(mediaType.getExamples()).isNotEmpty();
            String example = mediaType.getExamples().values().iterator().next().getValue().toString();
            assertThat(example).contains("401");
            assertThat(example).contains("Invalid username or password");
        }
    }

    @Nested
    @DisplayName("Region endpoints")
    class RegionEndpoints {

        @Test
        @DisplayName("GET /api/v1/regions documents 200 and 401")
        void getAllRegionsHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/regions");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401");
            assertThat(responses.get("200").getDescription()).contains("Paginated list");
        }

        @Test
        @DisplayName("GET /api/v1/regions 200 response has example payload")
        void getAllRegionsSuccessHasExample() {
            PathItem path = getPath("/api/v1/regions");
            ApiResponse success = path.getGet().getResponses().get("200");
            Content content = success.getContent();

            assertThat(content).containsKey("application/json");
            MediaType mediaType = content.get("application/json");
            assertThat(mediaType.getExamples()).isNotEmpty();
            String example = mediaType.getExamples().values().iterator().next().getValue().toString();
            assertThat(example).contains("content");
            assertThat(example).contains("totalElements");
        }

        @Test
        @DisplayName("GET /api/v1/regions/{id} documents 200, 401, 404")
        void getRegionByIdHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/regions/{id}");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401", "404");
            assertThat(responses.get("404").getDescription()).contains("not found");
        }

        @Test
        @DisplayName("POST /api/v1/regions documents 201, 400, 401, 403")
        void createRegionHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/regions");
            ApiResponses responses = path.getPost().getResponses();

            assertThat(responses).containsKeys("201", "400", "401", "403");
            assertThat(responses.get("201").getDescription()).contains("created");
            assertThat(responses.get("403").getDescription()).contains("admin");
        }

        @Test
        @DisplayName("PUT /api/v1/regions/{id} documents 200, 400, 401, 403, 404")
        void updateRegionHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/regions/{id}");
            ApiResponses responses = path.getPut().getResponses();

            assertThat(responses).containsKeys("200", "400", "401", "403", "404");
        }

        @Test
        @DisplayName("DELETE /api/v1/regions/{id} documents 204, 401, 403, 404")
        void deleteRegionHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/regions/{id}");
            ApiResponses responses = path.getDelete().getResponses();

            assertThat(responses).containsKeys("204", "401", "403", "404");
        }
    }

    @Nested
    @DisplayName("Flood Event endpoints")
    class FloodEventEndpoints {

        @Test
        @DisplayName("GET /api/v1/flood-events documents 200 and 401")
        void getAllFloodEventsHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/flood-events");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401");
        }

        @Test
        @DisplayName("GET /api/v1/flood-events 200 response has example payload")
        void getAllFloodEventsSuccessHasExample() {
            PathItem path = getPath("/api/v1/flood-events");
            ApiResponse success = path.getGet().getResponses().get("200");
            Content content = success.getContent();

            assertThat(content).containsKey("application/json");
            MediaType mediaType = content.get("application/json");
            assertThat(mediaType.getExamples()).isNotEmpty();
            String example = mediaType.getExamples().values().iterator().next().getValue().toString();
            assertThat(example).contains("riskScore");
            assertThat(example).contains("severity");
        }

        @Test
        @DisplayName("GET /api/v1/flood-events/{id} documents 200, 401, 404")
        void getFloodEventByIdHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/flood-events/{id}");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401", "404");
        }

        @Test
        @DisplayName("GET /api/v1/flood-events/high-risk documents 200 and 401")
        void getHighRiskHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/flood-events/high-risk");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401");
        }

        @Test
        @DisplayName("GET /api/v1/flood-events/high-risk 200 response has example payload")
        void getHighRiskSuccessHasExample() {
            PathItem path = getPath("/api/v1/flood-events/high-risk");
            ApiResponse success = path.getGet().getResponses().get("200");
            Content content = success.getContent();

            assertThat(content).containsKey("application/json");
            MediaType mediaType = content.get("application/json");
            assertThat(mediaType.getExamples()).isNotEmpty();
            String example = mediaType.getExamples().values().iterator().next().getValue().toString();
            assertThat(example).contains("riskScore");
        }

        @Test
        @DisplayName("POST /api/v1/flood-events documents 201, 400, 401, 403")
        void createFloodEventHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/flood-events");
            ApiResponses responses = path.getPost().getResponses();

            assertThat(responses).containsKeys("201", "400", "401", "403");
        }

        @Test
        @DisplayName("PUT /api/v1/flood-events/{id} documents 200, 400, 401, 403, 404")
        void updateFloodEventHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/flood-events/{id}");
            ApiResponses responses = path.getPut().getResponses();

            assertThat(responses).containsKeys("200", "400", "401", "403", "404");
        }

        @Test
        @DisplayName("DELETE /api/v1/flood-events/{id} documents 204, 401, 403, 404")
        void deleteFloodEventHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/flood-events/{id}");
            ApiResponses responses = path.getDelete().getResponses();

            assertThat(responses).containsKeys("204", "401", "403", "404");
        }
    }

    @Nested
    @DisplayName("Observation endpoints")
    class ObservationEndpoints {

        @Test
        @DisplayName("GET /api/v1/observations documents 200 and 401")
        void getAllObservationsHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/observations");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401");
        }

        @Test
        @DisplayName("GET /api/v1/observations 200 response has example payload")
        void getAllObservationsSuccessHasExample() {
            PathItem path = getPath("/api/v1/observations");
            ApiResponse success = path.getGet().getResponses().get("200");
            Content content = success.getContent();

            assertThat(content).containsKey("application/json");
            MediaType mediaType = content.get("application/json");
            assertThat(mediaType.getExamples()).isNotEmpty();
            String example = mediaType.getExamples().values().iterator().next().getValue().toString();
            assertThat(example).contains("temperature");
            assertThat(example).contains("rainfall");
        }

        @Test
        @DisplayName("GET /api/v1/observations/{id} documents 200, 401, 404")
        void getObservationByIdHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/observations/{id}");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401", "404");
        }

        @Test
        @DisplayName("GET /api/v1/observations/region/{regionId} documents 200, 401, 404")
        void getObservationsByRegionHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/observations/region/{regionId}");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401", "404");
        }

        @Test
        @DisplayName("GET /api/v1/observations/region/{regionId}/stats documents 200, 401, 404")
        void getObservationStatsHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/observations/region/{regionId}/stats");
            ApiResponses responses = path.getGet().getResponses();

            assertThat(responses).containsKeys("200", "401", "404");
        }

        @Test
        @DisplayName("GET /api/v1/observations/region/{regionId}/stats 200 response has example payload")
        void getObservationStatsSuccessHasExample() {
            PathItem path = getPath("/api/v1/observations/region/{regionId}/stats");
            ApiResponse success = path.getGet().getResponses().get("200");
            Content content = success.getContent();

            assertThat(content).containsKey("application/json");
            MediaType mediaType = content.get("application/json");
            assertThat(mediaType.getExamples()).isNotEmpty();
            String example = mediaType.getExamples().values().iterator().next().getValue().toString();
            assertThat(example).contains("avgTemperature");
            assertThat(example).contains("totalRainfall");
        }

        @Test
        @DisplayName("POST /api/v1/observations documents 201, 400, 401, 403")
        void createObservationHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/observations");
            ApiResponses responses = path.getPost().getResponses();

            assertThat(responses).containsKeys("201", "400", "401", "403");
        }

        @Test
        @DisplayName("DELETE /api/v1/observations/{id} documents 204, 401, 403, 404")
        void deleteObservationHasDocumentedResponses() {
            PathItem path = getPath("/api/v1/observations/{id}");
            ApiResponses responses = path.getDelete().getResponses();

            assertThat(responses).containsKeys("204", "401", "403", "404");
        }
    }
}
