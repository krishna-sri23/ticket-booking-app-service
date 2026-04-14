package com.bookingplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bookingPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Movie Ticket Booking Platform API")
                        .version("v1")
                        .description("Backend service for the movie ticket booking platform. " +
                                "Supports browsing theatres/shows, booking tickets with configurable offers.")
                        .contact(new Contact().name("Krishna").email("krishna@example.com")));
    }
}
