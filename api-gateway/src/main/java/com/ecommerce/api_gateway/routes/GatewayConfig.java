package com.ecommerce.api_gateway.routes;

import com.ecommerce.api_gateway.dto.ResponseDTO;
import com.ecommerce.api_gateway.filter.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config.setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service"))
                        )
                        .uri("lb://user-service"))
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f
                                .filter(jwtAuthorizationFilter.apply(new JwtAuthorizationFilter.Config()))
                                .circuitBreaker(config -> config.setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order-service"))
                        )
                        .uri("lb://order-service"))
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f
                                .filter(jwtAuthorizationFilter.apply(new JwtAuthorizationFilter.Config()))
                                .circuitBreaker(config -> config.setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product-service"))
                        )
                        .uri("lb://product-service"))
                .route("inventory-service", r -> r.path("/api/inventory/**")
                        .filters(f -> f
                                .filter(jwtAuthorizationFilter.apply(new JwtAuthorizationFilter.Config()))
                                .circuitBreaker(config -> config.setName("inventoryServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/inventory-service"))
                        )
                        .uri("lb://inventory-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return RouterFunctions.route(RequestPredicates.GET("/fallback/{service}")
                        .or(RequestPredicates.POST("/fallback/{service}")),
                request -> {
                    String service = request.pathVariable("service");
                    ResponseDTO errorResponse = ResponseDTO.builder()
                            .errorCode(503)
                            .service(service)
                            .errorMessage("API GATEWAY: Service Unavailable, please try again later!")
                            .build();
                    return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .bodyValue(errorResponse);
                });
    }

//    @Bean
//    public RouterFunction<ServerResponse> fallbackServiceRouter() {
//        return RouterFunctions.route()
//                .path("/api/inventory/**", builder -> builder.route(RequestPredicates.all(),
//                request -> fallbackResponse("inventory-service")))
//                .path("/api/products/**", builder -> builder.route(RequestPredicates.all(),
//                        request -> fallbackResponse("product-service")))
//                .path("/api/orders/**", builder -> builder.route(RequestPredicates.all(),
//                        request -> fallbackResponse("order-service")))
//                .path("/api/auth/**", builder -> builder.route(RequestPredicates.all(),
//                        request -> fallbackResponse("user-service")))
//                .path("/api/payment/**", builder -> builder.route(RequestPredicates.all(),
//                        request -> fallbackResponse("payment-service")))
//                .build();
//    }
//
//    private Mono<ServerResponse> fallbackResponse(String serviceName) {
//        Map<String, String> errorResponse = new HashMap<>();
//        errorResponse.put("errorCode", String.valueOf(org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE));
//        errorResponse.put("service", serviceName);
//        errorResponse.put("errorMessage", "Service Unavailable, please try again later!");
//        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
//                .body(Mono.just(errorResponse), Map.class);
//    }

}
