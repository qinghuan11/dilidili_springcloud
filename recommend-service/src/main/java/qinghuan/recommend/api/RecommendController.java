package qinghuan.recommend.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qinghuan.common.JwtUtil;
import qinghuan.common.Result;
import qinghuan.recommend.service.RecommendService;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

    private final RecommendService recommendService;
    private final JwtUtil jwtUtil;

    @PostMapping("/behavior")
    public ResponseEntity<Result<String>> recordBehavior(
            @RequestParam Long videoId,
            @RequestParam String action,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理行为记录请求：userId={}, videoId={}, action={}", userId, videoId, action);
        Result<String> result = recommendService.recordBehavior(userId, videoId, action);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<Result<List<Long>>> getRecommendations(
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理推荐请求：userId={}, size={}", userId, size);
        Result<List<Long>> result = recommendService.getRecommendations(userId, size);
        return ResponseEntity.ok(result);
    }
}
