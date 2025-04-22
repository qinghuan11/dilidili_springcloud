package qinghuan.video.api;

import qinghuan.common.Result;
import qinghuan.dao.domain.Video;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import qinghuan.video.service.VideoService;

/**
 * 视频相关接口
 */
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
    private final VideoService videoService;

    /**
     * 上传视频
     *
     * @param file        视频文件
     * @param title       视频标题
     * @param description 视频描述
     * @param request     HTTP 请求，包含授权头
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Result<String>> uploadVideo(
            @RequestPart MultipartFile file,
            @RequestPart @NotBlank(message = "Title cannot be empty") String title,
            @RequestPart String description,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        logger.info("处理视频上传请求：title={}", title);
        Result<String> result = videoService.uploadVideo(file, title, description, token);
        logger.debug("视频上传成功：title={}", title);
        return ResponseEntity.ok(result);
    }

    /**
     * 播放视频
     *
     * @param id 视频ID
     * @return 视频资源
     */
    @GetMapping(value = "/{id}/play", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> playVideo(@PathVariable Long id) {
        logger.info("处理视频播放请求：id={}", id);
        FileSystemResource resource = videoService.playVideo(id);
        if (resource == null) {
            logger.warn("视频未找到：id={}", id);
            return ResponseEntity.notFound().build();
        }
        logger.debug("视频播放成功：id={}", id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }

    /**
     * 点赞视频
     *
     * @param id      视频ID
     * @param request HTTP 请求，包含授权头
     * @return 点赞结果
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Result<String>> likeVideo(
            @PathVariable Long id,
            HttpServletRequest request) {
        logger.info("处理视频点赞请求：id={}", id);
        Result<String> result = videoService.likeVideo(id);
        logger.debug("视频点赞成功：id={}", id);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询视频列表
     *
     * @param page   页码
     * @param size   每页大小
     * @param sortBy 排序字段（viewCount 或 likeCount）
     * @param order  排序方式（asc 或 desc）
     * @return 视频分页列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result<Page<Video>>> listVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "viewCount") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        logger.info("处理视频列表请求：page={}, size={}", page, size);
        Result<Page<Video>> result = videoService.listVideos(page, size, sortBy, order);
        logger.debug("视频列表查询成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索视频
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页大小
     * @param sortBy  排序字段（viewCount 或 likeCount）
     * @param order   排序方式（asc 或 desc）
     * @return 视频分页列表
     */
    @GetMapping("/search")
    public ResponseEntity<Result<Page<Video>>> searchVideos(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "viewCount") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        logger.info("处理视频搜索请求：keyword={}, page={}, size={}", keyword, page, size);
        Result<Page<Video>> result = videoService.searchVideos(keyword, page, size, sortBy, order);
        logger.debug("视频搜索成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 删除视频
     *
     * @param id      视频ID
     * @param request HTTP 请求，包含授权头
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> deleteVideo(
            @PathVariable Long id,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        logger.info("处理视频删除请求：id={}", id);
        Result<String> result = videoService.deleteVideo(id, token);
        logger.debug("视频删除成功：id={}", id);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新视频
     *
     * @param id      视频ID
     * @param request HTTP 请求，包含授权头
     * @param title   视频标题
     * @param description 视频描述
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<String>> updateVideo(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestParam @NotBlank(message = "Title cannot be empty") String title,
            @RequestParam String description) {
        String token = (String) request.getAttribute("token");
        logger.info("处理视频更新请求：id={}", id);
        Result<String> result = videoService.updateVideo(id, title, description, token);
        logger.debug("视频更新成功：id={}", id);
        return ResponseEntity.ok(result);
    }
}