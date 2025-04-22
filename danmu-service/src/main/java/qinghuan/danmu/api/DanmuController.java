package qinghuan.danmu.api;

import qinghuan.common.Result;
import qinghuan.dao.domain.Danmu;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qinghuan.danmu.service.DanmuService;

import java.util.List;

/**
 * 弹幕相关接口
 */
@RestController
@RequestMapping("/api/danmu")
@RequiredArgsConstructor
public class DanmuController {
    private static final Logger logger = LoggerFactory.getLogger(DanmuController.class);
    private final DanmuService danmuService;

    /**
     * 发送弹幕
     *
     * @param videoId 视频ID
     * @param content 弹幕内容
     * @param request HTTP 请求，包含授权头
     * @return 发送结果
     */
    @PostMapping("/send")
    public ResponseEntity<Result<String>> sendDanmu(
            @RequestParam Long videoId,
            @RequestParam String content,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        logger.info("处理弹幕发送请求：videoId={}, content={}", videoId, content);
        Result<String> result = danmuService.sendDanmu(videoId, content, token);
        logger.debug("弹幕发送成功：videoId={}", videoId);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取视频的弹幕列表
     *
     * @param videoId 视频ID
     * @return 弹幕列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result<List<Danmu>>> listDanmu(@RequestParam Long videoId) {
        logger.info("处理弹幕列表请求：videoId={}", videoId);
        Result<List<Danmu>> result = danmuService.listDanmu(videoId);
        logger.debug("弹幕列表查询成功：videoId={}", videoId);
        return ResponseEntity.ok(result);
    }
}
