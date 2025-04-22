package qinghuan.video.service;

import qinghuan.common.JwtUtil;
import qinghuan.common.Result;
import qinghuan.dao.domain.Video;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import qinghuan.video.dao.mapper.VideoMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 视频服务实现
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);
    private static final String VIDEO_CACHE_KEY_PREFIX = "video:id:";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("mp4", "avi", "mkv");

    @Value("${video.storage.path}")
    private String storagePath;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<String> uploadVideo(MultipartFile file, String title, String description, String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isValidExtension(originalFilename)) {
                logger.warn("视频上传失败：文件类型无效，filename={}", originalFilename);
                throw new IllegalArgumentException("Invalid file type. Allowed types: " + ALLOWED_EXTENSIONS);
            }
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;
            String filePath = storagePath + filename;
            File directory = new File(storagePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            file.transferTo(new File(filePath));
            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setFilePath(filename);
            video.setUserId(userId);
            video.setViewCount(0L);
            video.setLikeCount(0L);
            save(video);
            cacheVideo(video);
            logger.info("视频上传成功：filename={}, userId={}", filename, userId);
            return Result.success(filename);
        } catch (IOException e) {
            logger.error("视频上传失败：error={}", e.getMessage());
            throw new IllegalArgumentException("视频上传失败：" + e.getMessage(), e);
        }
    }

    @Override
    public FileSystemResource playVideo(Long id) {
        try {
            incrementViewCount(id);
            Video video = getById(id);
            if (video == null) {
                logger.warn("视频未找到：id={}", id);
                return null;
            }
            return new FileSystemResource(storagePath + video.getFilePath());
        } catch (Exception e) {
            logger.error("视频播放失败：id={}, error={}", id, e.getMessage());
            throw new IllegalArgumentException("视频播放失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> likeVideo(Long id) {
        try {
            incrementLikeCount(id);
            logger.info("视频点赞成功：id={}", id);
            return Result.success("Video liked successfully");
        } catch (Exception e) {
            logger.error("视频点赞失败：id={}, error={}", id, e.getMessage());
            throw new IllegalArgumentException("视频点赞失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<Page<Video>> listVideos(int page, int size, String sortBy, String order) {
        try {
            Page<Video> videoPage = new Page<>(page, size);
            QueryWrapper<Video> wrapper = new QueryWrapper<>();
            wrapper.orderBy(true, "asc".equalsIgnoreCase(order), sortBy);
            page(videoPage, wrapper);
            logger.info("视频列表查询成功：page={}, size={}", page, size);
            return Result.success(videoPage);
        } catch (Exception e) {
            logger.error("视频列表查询失败：error={}", e.getMessage());
            throw new IllegalArgumentException("视频列表查询失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<Page<Video>> searchVideos(String keyword, int page, int size, String sortBy, String order) {
        try {
            Page<Video> videoPage = new Page<>(page, size);
            QueryWrapper<Video> wrapper = new QueryWrapper<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("title", keyword);
            }
            wrapper.orderBy(true, "asc".equalsIgnoreCase(order), sortBy);
            page(videoPage, wrapper);
            logger.info("视频搜索成功：keyword={}, page={}, size={}", keyword, page, size);
            return Result.success(videoPage);
        } catch (Exception e) {
            logger.error("视频搜索失败：error={}", e.getMessage());
            throw new IllegalArgumentException("视频搜索失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> deleteVideo(Long id, String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            Video video = getById(id);
            if (video == null) {
                logger.warn("视频删除失败：视频未找到，id={}", id);
                throw new IllegalArgumentException("Video not found");
            }
            if (!video.getUserId().equals(userId)) {
                logger.warn("视频删除失败：无权限，id={}, userId={}", id, userId);
                throw new IllegalArgumentException("You can only delete your own videos");
            }
            File file = new File(storagePath + video.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            removeById(id);
            evictVideoCache(id);
            logger.info("视频删除成功：id={}, userId={}", id, userId);
            return Result.success("Video deleted successfully");
        } catch (Exception e) {
            logger.error("视频删除失败：id={}, error={}", id, e.getMessage());
            throw new IllegalArgumentException("视频删除失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> updateVideo(Long id, String title, String description, String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            Video video = getById(id);
            if (video == null) {
                logger.warn("视频更新失败：视频未找到，id={}", id);
                throw new IllegalArgumentException("Video not found");
            }
            if (!video.getUserId().equals(userId)) {
                logger.warn("视频更新失败：无权限，id={}, userId={}", id, userId);
                throw new IllegalArgumentException("You can only update your own videos");
            }
            video.setTitle(title);
            video.setDescription(description);
            updateById(video);
            evictVideoCache(id);
            logger.info("视频更新成功：id={}, userId={}", id, userId);
            return Result.success("Video updated successfully");
        } catch (Exception e) {
            logger.error("视频更新失败：id={}, error={}", id, e.getMessage());
            throw new IllegalArgumentException("视频更新失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取视频
     *
     * @param id 视频ID
     * @return 视频对象，未找到返回 null
     */
    private Video getById(Long id) {
        String cacheKey = VIDEO_CACHE_KEY_PREFIX + id;
        Video cachedVideo = (Video) redisTemplate.opsForValue().get(cacheKey);
        if (cachedVideo != null) {
            logger.debug("从缓存获取视频：id={}", id);
            return cachedVideo;
        }
        Video video = super.getById(id);
        if (video != null) {
            cacheVideo(video);
        }
        return video;
    }

    /**
     * 增加播放量
     *
     * @param id 视频ID
     */
    private void incrementViewCount(Long id) {
        Video video = getById(id);
        if (video != null) {
            video.setViewCount(video.getViewCount() + 1);
            updateById(video);
            evictVideoCache(id);
            logger.debug("增加视频播放量：id={}", id);
        }
    }

    /**
     * 增加点赞数
     *
     * @param id 视频ID
     */
    private void incrementLikeCount(Long id) {
        Video video = getById(id);
        if (video != null) {
            video.setLikeCount(video.getLikeCount() + 1);
            updateById(video);
            evictVideoCache(id);
            logger.debug("增加视频点赞数：id={}", id);
        }
    }

    /**
     * 缓存视频信息到 Redis
     *
     * @param video 视频对象
     */
    private void cacheVideo(Video video) {
        String cacheKey = VIDEO_CACHE_KEY_PREFIX + video.getId();
        try {
            redisTemplate.opsForValue().set(cacheKey, video, 1, TimeUnit.HOURS);
            logger.debug("视频已缓存：id={}", video.getId());
        } catch (Exception e) {
            logger.error("缓存视频失败：id={}, error={}", video.getId(), e.getMessage());
        }
    }

    /**
     * 清除视频缓存
     *
     * @param id 视频ID
     */
    private void evictVideoCache(Long id) {
        String cacheKey = VIDEO_CACHE_KEY_PREFIX + id;
        try {
            redisTemplate.delete(cacheKey);
            logger.debug("视频缓存已清除：id={}", id);
        } catch (Exception e) {
            logger.error("清除视频缓存失败：id={}, error={}", id, e.getMessage());
        }
    }

    /**
     * 校验文件扩展名
     *
     * @param filename 文件名
     * @return 是否有效
     */
    private boolean isValidExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}