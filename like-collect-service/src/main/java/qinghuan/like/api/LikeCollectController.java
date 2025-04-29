package qinghuan.like.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qinghuan.common.JwtUtil;
import qinghuan.common.Result;
import qinghuan.dao.domain.Collect;
import qinghuan.like.service.LikeCollectService;

@RestController
@RequestMapping("/api/like-collect")
@RequiredArgsConstructor
public class LikeCollectController {
    private static final Logger logger = LoggerFactory.getLogger(LikeCollectController.class);

    private final LikeCollectService likeCollectService;
    private final JwtUtil jwtUtil;

    @PostMapping("/like")
    public ResponseEntity<Result<String>> likeVideo(
            @RequestParam Long videoId,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理点赞请求：userId={}, videoId={}", userId, videoId);
        Result<String> result = likeCollectService.likeVideo(userId, videoId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/unlike")
    public ResponseEntity<Result<String>> unlikeVideo(
            @RequestParam Long videoId,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理取消点赞请求：userId={}, videoId={}", userId, videoId);
        Result<String> result = likeCollectService.unlikeVideo(userId, videoId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/collect")
    public ResponseEntity<Result<String>> collectVideo(
            @RequestParam Long videoId,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理收藏请求：userId={}, videoId={}", userId, videoId);
        Result<String> result = likeCollectService.collectVideo(userId, videoId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/uncollect")
    public ResponseEntity<Result<String>> uncollectVideo(
            @RequestParam Long videoId,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理取消收藏请求：userId={}, videoId={}", userId, videoId);
        Result<String> result = likeCollectService.uncollectVideo(userId, videoId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/collections")
    public ResponseEntity<Result<Page<Collect>>> listCollections(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理收藏列表请求：userId={}, page={}, size={}", userId, page, size);
        Result<Page<Collect>> result = likeCollectService.listCollections(userId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/like-count")
    public ResponseEntity<Result<Long>> getLikeCount(@RequestParam Long videoId) {
        logger.info("处理点赞数查询请求：videoId={}", videoId);
        Result<Long> result = likeCollectService.getLikeCount(videoId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/collect-count")
    public ResponseEntity<Result<Long>> getCollectCount(@RequestParam Long videoId) {
        logger.info("处理收藏数查询请求：videoId={}", videoId);
        Result<Long> result = likeCollectService.getCollectCount(videoId);
        return ResponseEntity.ok(result);
    }
}
