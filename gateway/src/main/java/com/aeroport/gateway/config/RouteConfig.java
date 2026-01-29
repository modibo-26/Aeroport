package com.aeroport.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> authRoute() {
        return route("auth-service")
                .route(path("/auth/**"), http())
                .filter(lb("SERVICE-AUTH"))
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> volsRoute() {
        return route("vols-service")
                .route(path("/vols/**"), http())
                .filter(lb("SERVICE-VOLS"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> reservationsRoute() {
        return route("reservations-service")
                .route(path("/reservations/**"), http())
                .filter(lb("SERVICE-RESERVATIONS"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationsRoute() {
        return route("notifications-service")
                .route(path("/notifications/**"), http())
                .filter(lb("SERVICE-NOTIFICATIONS"))
                .build();
    }
}