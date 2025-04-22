package qinghuan.auth.service;

import qinghuan.common.JwtUtil;
import qinghuan.common.Result;
import qinghuan.dao.domain.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import qinghuan.auth.dao.mapper.UserMapper;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String USER_CACHE_KEY_PREFIX = "user:id:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<String> register(String username, String password) {
        try {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", username);
            if (count(wrapper) > 0) {
                logger.warn("用户注册失败：用户名已存在，username={}", username);
                throw new IllegalArgumentException("Username already exists");
            }
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            save(user);
            logger.info("用户注册成功：username={}", username);
            return Result.success("User registered successfully");
        } catch (Exception e) {
            logger.error("用户注册失败：username={}, error={}", username, e.getMessage());
            throw new IllegalArgumentException("用户注册失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> login(String username, String password) {
        try {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", username);
            User user = getOne(wrapper);
            if (user == null) {
                logger.warn("用户登录失败：用户未找到，username={}", username);
                throw new IllegalArgumentException("User not found");
            }
            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("用户登录失败：密码错误，username={}", username);
                throw new IllegalArgumentException("Incorrect password");
            }
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            cacheUser(user);
            logger.info("用户登录成功：username={}", username);
            return Result.success(token);
        } catch (Exception e) {
            logger.error("用户登录失败：username={}, error={}", username, e.getMessage());
            throw new IllegalArgumentException("用户登录失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<User> getUserInfo(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String cacheKey = USER_CACHE_KEY_PREFIX + userId;
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                logger.debug("从缓存获取用户信息：userId={}", userId);
                return Result.success(cachedUser);
            }
            User user = getById(userId);
            if (user == null) {
                logger.warn("用户信息查询失败：用户未找到，userId={}", userId);
                throw new IllegalArgumentException("User not found");
            }
            cacheUser(user);
            logger.info("用户信息查询成功：userId={}", userId);
            return Result.success(user);
        } catch (Exception e) {
            logger.error("用户信息查询失败：error={}", e.getMessage());
            throw new IllegalArgumentException("用户信息查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 缓存用户信息到 Redis
     *
     * @param user 用户对象
     */
    private void cacheUser(User user) {
        String cacheKey = USER_CACHE_KEY_PREFIX + user.getId();
        try {
            redisTemplate.opsForValue().set(cacheKey, user, 1, TimeUnit.HOURS);
            logger.debug("用户已缓存：id={}", user.getId());
        } catch (Exception e) {
            logger.error("缓存用户失败：id={}, error={}", user.getId(), e.getMessage());
        }
    }
}