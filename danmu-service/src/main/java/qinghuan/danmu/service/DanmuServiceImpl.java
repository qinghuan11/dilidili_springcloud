package qinghuan.danmu.service;

import qinghuan.common.JwtUtil;
import qinghuan.common.Result;
import qinghuan.dao.domain.Danmu;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import qinghuan.danmu.controller.DanmuWebSocketHandler;
import qinghuan.danmu.dao.mapper.DanmuMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 弹幕服务实现
 */
@Service
public class DanmuServiceImpl extends ServiceImpl<DanmuMapper, Danmu> implements DanmuService {
    private static final Logger logger = LoggerFactory.getLogger(DanmuServiceImpl.class);
    private static final String DANMU_CACHE_KEY_PREFIX = "danmu:video:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DanmuWebSocketHandler danmuWebSocketHandler;

    @Override
    public Result<String> sendDanmu(Long videoId, String content, String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            Danmu danmu = new Danmu();
            danmu.setVideoId(videoId);
            danmu.setUserId(userId);
            danmu.setContent(content);
            save(danmu);
            evictDanmuCache(videoId);
            danmuWebSocketHandler.broadcastDanmu(danmu);
            logger.info("弹幕发送成功：videoId={}, userId={}", videoId, userId);
            return Result.success("Danmu sent successfully");
        } catch (Exception e) {
            logger.error("弹幕发送失败：videoId={}, error={}", videoId, e.getMessage());
            throw new IllegalArgumentException("弹幕发送失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<List<Danmu>> listDanmu(Long videoId) {
        try {
            String cacheKey = DANMU_CACHE_KEY_PREFIX + videoId;
            @SuppressWarnings("unchecked")
            List<Danmu> cachedDanmuList = (List<Danmu>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedDanmuList != null) {
                logger.debug("从缓存获取弹幕列表：videoId={}", videoId);
                return Result.success(cachedDanmuList);
            }
            QueryWrapper<Danmu> wrapper = new QueryWrapper<>();
            wrapper.eq("video_id", videoId);
            List<Danmu> danmuList = list(wrapper);
            redisTemplate.opsForValue().set(cacheKey, danmuList, 1, TimeUnit.HOURS);
            logger.info("弹幕列表查询成功：videoId={}", videoId);
            return Result.success(danmuList);
        } catch (Exception e) {
            logger.error("弹幕列表查询失败：videoId={}, error={}", videoId, e.getMessage());
            throw new IllegalArgumentException("弹幕列表查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 清除弹幕缓存
     *
     * @param videoId 视频ID
     */
    private void evictDanmuCache(Long videoId) {
        String cacheKey = DANMU_CACHE_KEY_PREFIX + videoId;
        try {
            redisTemplate.delete(cacheKey);
            logger.debug("弹幕缓存已清除：videoId={}", videoId);
        } catch (Exception e) {
            logger.error("清除弹幕缓存失败：videoId={}, error={}", videoId, e.getMessage());
        }
    }
}