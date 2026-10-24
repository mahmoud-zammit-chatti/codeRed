package com.mahmoud.api_gateway.Configs;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway configuration to explicitly define routes and filters
 * This ensures routes take precedence over static resource handlers
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("incident-service", r -> r
                        .path("/api/incidents/**")
                        .uri("https://localhost:8081"))
                .route("dispatch-service", r -> r
                        .path("/api/dispatch/**")
                        .uri("http://localhost:8082"))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://locahost:8084"))
                .build();
    }
}
