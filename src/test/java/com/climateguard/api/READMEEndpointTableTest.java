package com.climateguard.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the README endpoint table stays in sync with the actual
 * controller mappings. Run this test after adding or removing endpoints.
 */
class READMEEndpointTableTest {

    private static final Pattern ENDPOINT_ROW = Pattern.compile(
            "^\\|\\s*(GET|POST|PUT|DELETE)\\s*\\|\\s*(/api/v1/[^\\s|]+)\\s*\\|\\s*(Public|USER|ADMIN)\\s*\\|");

    @Test
    void readmeEndpointTableMatchesControllers() throws IOException {
        String readme = Files.readString(findReadme());
        List<String[]> rows = parseEndpointRows(readme);

        // --- Auth endpoints ---
        assertEndpoint(rows, "POST", "/api/v1/auth/register", "Public");
        assertEndpoint(rows, "POST", "/api/v1/auth/login", "Public");

        // --- Region endpoints ---
        assertEndpoint(rows, "GET", "/api/v1/regions", "USER");
        assertEndpoint(rows, "GET", "/api/v1/regions/{id}", "USER");
        assertEndpoint(rows, "POST", "/api/v1/regions", "ADMIN");
        assertEndpoint(rows, "PUT", "/api/v1/regions/{id}", "ADMIN");
        assertEndpoint(rows, "DELETE", "/api/v1/regions/{id}", "ADMIN");

        // --- Observation endpoints ---
        assertEndpoint(rows, "GET", "/api/v1/observations", "USER");
        assertEndpoint(rows, "GET", "/api/v1/observations/{id}", "USER");
        assertEndpoint(rows, "GET", "/api/v1/observations/region/{regionId}", "USER");
        assertEndpoint(rows, "GET", "/api/v1/observations/region/{regionId}/stats", "USER");
        assertEndpoint(rows, "POST", "/api/v1/observations", "ADMIN");
        assertEndpoint(rows, "DELETE", "/api/v1/observations/{id}", "ADMIN");

        // --- Flood event endpoints ---
        assertEndpoint(rows, "GET", "/api/v1/flood-events", "USER");
        assertEndpoint(rows, "GET", "/api/v1/flood-events/{id}", "USER");
        assertEndpoint(rows, "GET", "/api/v1/flood-events/region/{regionId}", "USER");
        assertEndpoint(rows, "GET", "/api/v1/flood-events/high-risk", "USER");
        assertEndpoint(rows, "POST", "/api/v1/flood-events", "ADMIN");
        assertEndpoint(rows, "PUT", "/api/v1/flood-events/{id}", "ADMIN");
        assertEndpoint(rows, "DELETE", "/api/v1/flood-events/{id}", "ADMIN");

        // Total should be exactly 20
        assertEquals(20, rows.size(),
                "Expected 20 endpoints in README table but found " + rows.size());
    }

    @Test
    void readmeDocumentsJava17() throws IOException {
        String readme = Files.readString(findReadme());
        assertTrue(readme.contains("Java 17"),
                "README must document the required Java version (Java 17).");
    }

    // --- helpers ---

    private static List<String[]> parseEndpointRows(String readme) {
        return readme.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("|"))
                .map(ENDPOINT_ROW::matcher)
                .filter(Matcher::find)
                .map(m -> new String[]{m.group(1), m.group(2), m.group(3)})
                .toList();
    }

    private static void assertEndpoint(List<String[]> rows, String method, String path, String auth) {
        boolean found = rows.stream()
                .anyMatch(r -> r[0].equals(method) && r[1].equals(path) && r[2].equals(auth));
        assertTrue(found, "Missing endpoint in README: " + method + " " + path + " (" + auth + ")");
    }

    private static Path findReadme() {
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 5 && dir != null; i++) {
            Path candidate = dir.resolve("README.md");
            if (Files.exists(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new AssertionError("README.md not found in current or parent directories.");
    }
}
