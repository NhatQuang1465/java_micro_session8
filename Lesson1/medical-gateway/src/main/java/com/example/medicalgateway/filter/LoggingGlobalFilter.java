package com.example.medicalgateway.filter;

import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Lấy địa chỉ IP máy khách (Remote Address)
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String ipAddress = (remoteAddress != null && remoteAddress.getAddress() != null)
                ? remoteAddress.getAddress().getHostAddress()
                : "Unknown IP";

        // 2. Lấy phương thức HTTP (GET, POST, PUT, DELETE,...)
        HttpMethod method = request.getMethod();

        // 3. Lấy đường dẫn (Path)
        String path = request.getPath().value();

        // Ghi log ra console
        logger.info("Global Filter - Remote Address: {}, Method: {}, Path: {}", ipAddress, method, path);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Đảm bảo filter chạy trước khi gửi request tới downstream service
    }
}
