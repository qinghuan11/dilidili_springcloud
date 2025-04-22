package qinghuan.auth.api;

import qinghuan.common.Result;
import qinghuan.dao.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qinghuan.auth.service.AuthService;

/**
 * 认证相关接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<Result<String>> register(
            @RequestParam @NotBlank(message = "Username cannot be empty") String username,
            @RequestParam @NotBlank(message = "Password cannot be empty") String password) {
        logger.info("处理用户注册请求：username={}", username);
        Result<String> result = authService.register(username, password);
        logger.debug("用户注册成功：username={}", username);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含 JWT 令牌
     */
    @PostMapping("/login")
    public ResponseEntity<Result<String>> login(
            @RequestParam @NotBlank(message = "Username cannot be empty") String username,
            @RequestParam @NotBlank(message = "Password cannot be empty") String password) {
        logger.info("处理用户登录请求：username={}", username);
        Result<String> result = authService.login(username, password);
        logger.debug("用户登录成功：username={}", username);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户信息
     *
     * @param request HTTP 请求，包含授权头
     * @return 用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<Result<User>> getUserInfo(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        logger.info("处理用户信息请求");
        Result<User> result = authService.getUserInfo(token);
        logger.debug("用户信息查询成功");
        return ResponseEntity.ok(result);
    }
}

