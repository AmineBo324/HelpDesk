
package com.helpdesk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helpdeskOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("HelpDesk API")
                        .description("""
                                REST API for managing support tickets,
                                users, categories and authentication.

                                Features:
                                - JWT authentication
                                - Role-based authorization
                                - Ticket management
                                - Filtering and pagination
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HelpDesk API")
                                .email("support@helpdesk.com")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
