package com.ecommerce.api_gateway.filter;

import com.ecommerce.api_gateway.dto.ResponseDTO;
import com.ecommerce.api_gateway.utils.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtAuthorizationFilter extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {

    public static class Config {

    }

    public JwtAuthorizationFilter() {
        super(Config.class);
    }

    @Autowired
    private JwtUtils utils;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String token = getJwtFromRequest(exchange.getRequest());
                if (token != null && utils.validateJwtToken(token)) {
                    Claims claims = utils.getClaimsFromToken(token);
                    List<String> roles = claims.get("roles", List.class);

                    String path = exchange.getRequest().getURI().getPath();
                    if (!isAuthorized(roles, path)) {
                        return unauthorized(exchange, "You do not have permission to access this resource.");
                    }
                } else {
                    return unauthorized(exchange, "Missing or invalid Authorization header.");
                }
                ServerHttpRequest customerDetails = exchange.getRequest()
                        .mutate()
                        .header("customer", utils.getUsernameFromJwtToken(token))
                        .build();
                return chain.filter(exchange.mutate().request(customerDetails).build());
            };
        }

    private boolean isAuthorized(List<String> roles, String path) {
        if ((path.startsWith("/api/orders/viewAll") || path.startsWith("/api/orders/view")
                || path.startsWith("/api/orders/viewProducts"))) {
            return true;
        } else if ((path.startsWith("/api/orders/create") || path.startsWith("/api/orders/createPaymentLink")
                || path.startsWith("/api/orders/updateStatus")) && roles.contains("ROLE_CUSTOMER")) {
            return true;
        }
        if (path.startsWith("/api/products/view")) {
            return true;
        } else if ((path.startsWith("/api/products/add") || path.startsWith("api/products/edit")
                || path.startsWith("/api/products/delete")) && roles.contains("ROLE_ADMIN")) {
            return true;
        }
        if (path.startsWith("/api/inventory/all") || path.startsWith("/api/inventory/{product}")
                || path.startsWith("/api/inventory/available")) {
            return true;
        } else if ((path.startsWith("/api/inventory/upsert") || path.startsWith("/api/inventory/remove"))
                && roles.contains("ROLE_ADMIN")) {
            return true;
        } else if ((path.startsWith("/api/inventory/update") || path.startsWith("/api/inventory/increase"))
                && roles.contains("ROLE_CUSTOMER")) {
            return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ResponseDTO responseDTO = ResponseDTO.builder()
                .message("Authorization failed!")
                .errorMessage(message)
                .build();

        byte[] responseBytes;
        try {
            responseBytes = objectMapper.writeValueAsString(responseDTO).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error("Error serializing unauthorized response", e);
            responseBytes = new byte[0];
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String getJwtFromRequest(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            return authHeaders.get(0).replace("Bearer ", "");
        }
        return null;
    }
}
