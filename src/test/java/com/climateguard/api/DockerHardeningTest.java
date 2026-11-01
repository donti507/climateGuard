package com.climateguard.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerHardeningTest {

    @Test
    void dockerfileRunsAsNonRootUser() throws IOException {
        String dockerfile = read("Dockerfile");

        assertTrue(dockerfile.contains("adduser"), "Dockerfile should create a non-root user");
        assertTrue(dockerfile.contains("USER appuser"), "Dockerfile should switch to the non-root user");

        String lastUser = null;
        for (String line : dockerfile.lines().toArray(String[]::new)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("USER")) {
                lastUser = trimmed;
            }
        }

        assertNotNull(lastUser, "Dockerfile should contain a USER instruction");
        assertEquals("USER appuser", lastUser, "The final USER instruction should be the non-root user");
    }

    @Test
    void dockerComposeAppServiceHasHealthcheck() throws IOException {
        String compose = read("docker-compose.yml");

        int appIndex = compose.indexOf("  app:");
        assertTrue(appIndex >= 0, "docker-compose.yml should define the app service");

        String appSection = compose.substring(appIndex);
        assertTrue(appSection.contains("healthcheck:"), "app service should define a healthcheck");
        assertTrue(
                appSection.contains("curl -fsS --max-time 5 http://localhost:8080/actuator/health"),
                "app healthcheck should probe the actuator health endpoint"
        );
        assertTrue(appSection.contains("start_period: 60s"), "app healthcheck should allow startup time");
    }

    private static String read(String fileName) throws IOException {
        Path path = Path.of(fileName);
        assertTrue(Files.exists(path), fileName + " should exist");
        return Files.readString(path);
    }
}
