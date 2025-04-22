package qinghuan.auth.config;

import qinghuan.common.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 认证过滤器，用于验证请求中的 JWT 令牌
 */
@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/register",
            "/api/auth/login"
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        logger.info("处理请求路径：path={}", path);

        // 放行公开路径
        if (isPublicPath(path)) {
            logger.debug("公开路径，放行：path={}", path);
            chain.doFilter(request, response);
            return;
        }

        // 验证 Authorization 头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("未提供有效的 Authorization 头：path={}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 提取并验证 JWT 令牌
        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            logger.debug("JWT 令牌验证成功：userId={}", userId);

            // 将 token 添加到请求属性，供下游使用
            request.setAttribute("token", token);
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("JWT 令牌验证失败：path={}, error={}", path, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * 判断是否为公开路径
     *
     * @param path 请求路径
     * @return 是否为公开路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path);
    }
}