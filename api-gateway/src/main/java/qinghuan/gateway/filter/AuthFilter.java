package qinghuan.gateway.filter;

import qinghuan.gateway.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 认证过滤器，用于验证请求中的 JWT 令牌
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/users/register",
            "/api/users/login",
            "/api/videos/list",
            "/api/videos/search",
            "/api/videos/\\d+/play"
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        logger.info("处理请求路径：path={}", path);

        // 放行公开路径
        if (isPublicPath(path)) {
            logger.debug("公开路径，放行：path={}", path);
            return chain.filter(exchange);
        }

        // 验证 Authorization 头
        HttpHeaders headers = request.getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("未提供有效的 Authorization 头：path={}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 提取并验证 JWT 令牌
        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            logger.debug("JWT 令牌验证成功：userId={}", userId);

            // 将 token 添加到请求头，供下游服务使用
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            logger.error("JWT 令牌验证失败：path={}, error={}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100; // 确保认证过滤器优先执行
    }

    /**
     * 判断是否为公开路径
     *
     * @param path 请求路径
     * @return 是否为公开路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> path.matches(pattern));
    }
}
