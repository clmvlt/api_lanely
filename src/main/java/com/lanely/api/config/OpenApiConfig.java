package com.lanely.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:api-lanely}")
    private String applicationName;

    @Bean
    public OpenAPI lanelyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lanely API")
                        .description("""
                                REST API for Lanely.

                                Every endpoint is fully documented: HTTP method, path, request and \
                                response payloads (with field types, constraints and examples) and the \
                                full list of possible status codes. The documentation is intended to be \
                                self-sufficient so a route can be understood end to end from this page alone.""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Lanely")
                                .email("contact@lanely.fr"))
                        .license(new License()
                                .name("Proprietary")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token obtained from /auth/login, /auth/register or /auth/profile/login")));
    }
}
