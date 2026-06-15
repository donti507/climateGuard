package com.climateguard.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI climateGuardOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClimateGuard API")
                        .description("REST API for Climate Observations and Flood Risk Management. " +
                                "Covers real Polish regions including Siechnice and Klodzko from the September 2024 Central European Flood.")
                        .version("v1.0")
                        .contact(new Contact().name("ClimateGuard").email("admin@climateguard.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token")));
    }
}
