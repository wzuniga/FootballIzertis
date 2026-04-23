package com.izertis.football.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * OpenAPI 3.0 configuration.
 *
 * <p>Exposes interactive documentation at {@code /swagger-ui.html} and
 * the JSON spec at {@code /api-docs}.</p>
 *
 * <p>The {@code bearerAuth} security scheme allows entering the JWT token
 * directly in the Swagger UI "Authorize" dialog.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Enables shallow ETag support. The filter computes an MD5 hash of the response body
     * and returns 304 Not Modified when the client sends a matching If-None-Match header,
     * avoiding unnecessary payload transfer.
     */
    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Bean
    public OpenAPI footballConfederationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Football Confederation API")
                        .version("1.0.0")
                        .description("""
                                REST API for the Football Confederation Club & Player Registry.

                                **Authentication flow:**
                                1. Register via `POST /club`
                                2. Obtain a JWT token via `POST /login`
                                3. Click **Authorize** and enter `Bearer <your-token>`
                                4. Use the token in the `Authorization` header for all other requests
                                """)
                        .contact(new Contact()
                                .name("Football Confederation")
                                .email("api@confederation.football")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter the JWT token obtained from POST /login")));
    }
}
