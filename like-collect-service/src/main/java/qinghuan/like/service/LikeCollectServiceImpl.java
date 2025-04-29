package qinghuan.like.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import qinghuan.common.Result;
import qinghuan.dao.domain.Collect;
import qinghuan.dao.domain.Like;
import qinghuan.like.dao.mapper.CollectMapper;
import qinghuan.like.dao.mapper.LikeMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LikeCollectServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeCollectService {
    private static final Logger logger = LoggerFactory.getLogger(LikeCollectServiceImpl.class);
    private static final String LIKE_COUNT_KEY = "like:count:%d";
    private static final String COLLECT_COUNT_KEY = "collect:count:%d";

    private final LikeMapper likeMapper;
    private final CollectMapper collectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result<String> likeVideo(Long userId, Long videoId) {
        try {
            QueryWrapper<Like> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("video_id", videoId);
            if (likeMapper.selectCount(wrapper) > 0) {
                logger.warn("重复点赞：userId={}, videoId={}", userId, videoId);
                throw new IllegalArgumentException("Already liked");
            }
            Like like = new Like();
            like.setUserId(userId);
            like.setVideoId(videoId);
            likeMapper.insert(like);
            updateLikeCountCache(videoId);
            logger.info("点赞成功：userId={}, videoId={}", userId, videoId);
            return Result.success("Liked successfully");
        } catch (Exception e) {
            logger.error("点赞失败：userId={}, videoId={}, error={}", userId, videoId, e.getMessage());
            throw new IllegalArgumentException("Failed to like: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> unlikeVideo(Long userId, Long videoId) {
        try {
            QueryWrapper<Like> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("video_id", videoId);
            if (likeMapper.delete(wrapper) == 0) {
                logger.warn("取消点赞失败：未找到点赞记录，userId={}, videoId={}", userId, videoId);
                throw new IllegalArgumentException("Not liked");
            }
            updateLikeCountCache(videoId);
            logger.info("取消点赞成功：userId={}, videoId={}", userId, videoId);
            return Result.success("Unliked successfully");
        } catch (Exception e) {
            logger.error("取消点赞失败：userId={}, videoId={}, error={}", userId, videoId, e.getMessage());
            throw new IllegalArgumentException("Failed to unlike: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> collectVideo(Long userId, Long videoId) {
        try {
            QueryWrapper<Collect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("video_id", videoId);
            if (collectMapper.selectCount(wrapper) > 0) {
                logger.warn("重复收藏：userId={}, videoId={}", userId, videoId);
                throw new IllegalArgumentException("Already collected");
            }
            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setVideoId(videoId);
            collectMapper.insert(collect);
            updateCollectCountCache(videoId);
            logger.info("收藏成功：userId={}, videoId={}", userId, videoId);
            return Result.success("Collected successfully");
        } catch (Exception e) {
            logger.error("收藏失败：userId={}, videoId={}, error={}", userId, videoId, e.getMessage());
            throw new IllegalArgumentException("Failed to collect: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> uncollectVideo(Long userId, Long videoId) {
        try {
            QueryWrapper<Collect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("video_id", videoId);
            if (collectMapper.delete(wrapper) == 0) {
                logger.warn("取消收藏失败：未找到收藏记录，userId={}, videoId={}", userId, videoId);
                throw new IllegalArgumentException("Not collected");
            }
            updateCollectCountCache(videoId);
            logger.info("取消收藏成功：userId={}, videoId={}", userId, videoId);
            return Result.success("Uncollected successfully");
        } catch (Exception e) {
            logger.error("取消收藏失败：userId={}, videoId={}, error={}", userId, videoId, e.getMessage());
            throw new IllegalArgumentException("Failed to uncollect: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<Page<Collect>> listCollections(Long userId, int page, int size) {
        try {
            Page<Collect> collectPage = new Page<>(page, size);
            QueryWrapper<Collect> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).orderByDesc("created_at");
            collectMapper.selectPage(collectPage, wrapper);
            logger.info("收藏列表查询成功：userId={}, page={}, size={}", userId, page, size);
            return Result.success(collectPage);
        } catch (Exception e) {
            logger.error("收藏列表查询失败：userId={}, page={}, size={}, error={}", userId, page, size, e.getMessage());
            throw new IllegalArgumentException("Failed to list collections: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<Long> getLikeCount(Long videoId) {
        try {
            String cacheKey = String.format(LIKE_COUNT_KEY, videoId);
            String cachedCount = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCount != null) {
                logger.debug("从缓存获取点赞数：videoId={}", videoId);
                return Result.success(Long.valueOf(cachedCount));
            }
            QueryWrapper<Like> wrapper = new QueryWrapper<>();
            wrapper.eq("video_id", videoId);
            long count = likeMapper.selectCount(wrapper);
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(count), 1, TimeUnit.HOURS);
            logger.info("点赞数查询成功：videoId={}, count={}", videoId, count);
            return Result.success(count);
        } catch (Exception e) {
            logger.error("点赞数查询失败：videoId={}, error={}", videoId, e.getMessage());
            throw new IllegalArgumentException("Failed to get like count: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<Long> getCollectCount(Long videoId) {
        try {
            String cacheKey = String.format(COLLECT_COUNT_KEY, videoId);
            String cachedCount = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCount != null) {
                logger.debug("从缓存获取收藏数：videoId={}", videoId);
                return Result.success(Long.valueOf(cachedCount));
            }
            QueryWrapper<Collect> wrapper = new QueryWrapper<>();
            wrapper.eq("video_id", videoId);
            long count = collectMapper.selectCount(wrapper);
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(count), 1, TimeUnit.HOURS);
            logger.info("收藏数查询成功：videoId={}, count={}", videoId, count);
            return Result.success(count);
        } catch (Exception e) {
            logger.error("收藏数查询失败：videoId={}, error={}", videoId, e.getMessage());
            throw new IllegalArgumentException("Failed to get collect count: " + e.getMessage(), e);
        }
    }

    private void updateLikeCountCache(Long videoId) {
        try {
            QueryWrapper<Like> wrapper = new QueryWrapper<>();
            wrapper.eq("video_id", videoId);
            long count = likeMapper.selectCount(wrapper);
            String cacheKey = String.format(LIKE_COUNT_KEY, videoId);
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(count), 1, TimeUnit.HOURS);
            logger.debug("更新点赞数缓存：videoId={}, count={}", videoId, count);
        } catch (Exception e) {
            logger.error("更新点赞数缓存失败：videoId={}, error={}", videoId, e.getMessage());
        }
    }

    private void updateCollectCountCache(Long videoId) {
        try {
            QueryWrapper<Collect> wrapper = new QueryWrapper<>();
            wrapper.eq("video_id", videoId);
            long count = collectMapper.selectCount(wrapper);
            String cacheKey = String.format(COLLECT_COUNT_KEY, videoId);
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(count), 1, TimeUnit.HOURS);
            logger.debug("更新收藏数缓存：videoId={}, count={}", videoId, count);
        } catch (Exception e) {
            logger.error("更新收藏数缓存失败：videoId={}, error={}", videoId, e.getMessage());
        }
    }
}
