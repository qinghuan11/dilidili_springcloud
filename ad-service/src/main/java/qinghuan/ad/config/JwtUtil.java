package qinghuan.ad.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * JWT 工具类，用于生成和验证 JWT 令牌
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Autowired(required = false) // 防止 RedisTemplate 不可用时 Bean 创建失败
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成 JWT 令牌
     *
     * @param username 用户名
     * @param userId   用户ID
     * @return JWT 令牌
     * @throws IllegalArgumentException 如果用户名或密钥无效
     */
    public String generateToken(String username, Long userId) {
        if (!StringUtils.hasText(username)) {
            logger.warn("生成令牌失败：用户名为空");
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(secret)) {
            logger.error("生成令牌失败：JWT 密钥未配置");
            throw new IllegalArgumentException("JWT 密钥未配置");
        }
        try {
            String token = JWT.create()
                    .withSubject(username)
                    .withClaim("userId", userId)
                    .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                    .sign(Algorithm.HMAC256(secret));
            logger.debug("生成令牌成功：用户 {}", username);
            return token;
        } catch (Exception e) {
            logger.error("生成令牌失败：用户 {}, error={}", username, e.getMessage());
            throw new IllegalArgumentException("生成令牌失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证 JWT 令牌并获取用户名
     *
     * @param token JWT 令牌
     * @return 用户名
     * @throws JWTVerificationException 如果令牌无效
     * @throws IllegalArgumentException 如果令牌为空或在黑名单
     */
    public String verifyToken(String token) throws JWTVerificationException {
        if (!StringUtils.hasText(token)) {
            logger.warn("验证令牌失败：令牌为空");
            throw new IllegalArgumentException("令牌不能为空");
        }
        if (redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
            logger.warn("验证令牌失败：令牌已失效");
            throw new IllegalArgumentException("令牌已失效");
        }
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            String username = jwt.getSubject();
            logger.debug("验证令牌成功：用户 {}", username);
            return username;
        } catch (JWTVerificationException e) {
            logger.warn("验证令牌失败：error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从 JWT 令牌中获取用户 ID
     *
     * @param token JWT 令牌
     * @return 用户 ID
     * @throws JWTVerificationException 如果令牌无效
     * @throws IllegalArgumentException 如果令牌为空或在黑名单
     */
    public Long getUserIdFromToken(String token) throws JWTVerificationException {
        if (!StringUtils.hasText(token)) {
            logger.warn("获取用户ID失败：令牌为空");
            throw new IllegalArgumentException("令牌不能为空");
        }
        if (redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
            logger.warn("获取用户ID失败：令牌已失效");
            throw new IllegalArgumentException("令牌已失效");
        }
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            Long userId = jwt.getClaim("userId").asLong();
            logger.debug("获取用户ID成功：{}", userId);
            return userId;
        } catch (JWTVerificationException e) {
            logger.warn("获取用户ID失败：error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取令牌过期时间（毫秒）
     *
     * @return 过期时间
     */
    public long getExpirationTime() {
        return expirationTime;
    }
}