package qinghuan.recommend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import qinghuan.common.Result;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendServiceImpl.class);
    private static final String USER_BEHAVIOR_KEY = "user:behavior:%d";
    private static final String VIDEO_SCORE_KEY = "video:score:%d";
    private static final String RECOMMEND_CACHE_KEY = "recommend:user:%d";
    private static final double WATCH_SCORE = 1.0;
    private static final double LIKE_SCORE = 3.0;
    private static final double COMMENT_SCORE = 5.0;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result<String> recordBehavior(Long userId, Long videoId, String action) {
        try {
            String behaviorKey = String.format(USER_BEHAVIOR_KEY, userId);
            String videoScoreKey = String.format(VIDEO_SCORE_KEY, videoId);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String behaviorValue = action + ":" + timestamp;

            // 记录用户行为
            redisTemplate.opsForHash().put(behaviorKey, videoId.toString(), behaviorValue);

            // 更新视频评分
            double score = switch (action.toLowerCase()) {
                case "watch" -> WATCH_SCORE;
                case "like" -> LIKE_SCORE;
                case "comment" -> COMMENT_SCORE;
                default -> throw new IllegalArgumentException("Invalid action: " + action);
            };
            redisTemplate.opsForZSet().add(videoScoreKey, userId.toString(), score);

            // 清除推荐缓存
            String recommendKey = String.format(RECOMMEND_CACHE_KEY, userId);
            redisTemplate.delete(recommendKey);

            logger.info("行为记录成功：userId={}, videoId={}, action={}", userId, videoId, action);
            return Result.success("Behavior recorded successfully");
        } catch (Exception e) {
            logger.error("行为记录失败：userId={}, videoId={}, action={}, error={}", userId, videoId, action, e.getMessage());
            throw new IllegalArgumentException("Failed to record behavior: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<List<Long>> getRecommendations(Long userId, int size) {
        try {
            String recommendKey = String.format(RECOMMEND_CACHE_KEY, userId);
            List<Object> cachedRecommendations = redisTemplate.opsForList().range(recommendKey, 0, size - 1);
            if (cachedRecommendations != null && !cachedRecommendations.isEmpty()) {
                List<Long> cachedResult = cachedRecommendations.stream()
                        .map(obj -> Long.valueOf(obj.toString()))
                        .collect(Collectors.toList());
                logger.debug("从缓存获取推荐：userId={}", userId);
                return Result.success(cachedResult);
            }

            // 获取用户行为
            String behaviorKey = String.format(USER_BEHAVIOR_KEY, userId);
            Map<Object, Object> userBehaviors = redisTemplate.opsForHash().entries(behaviorKey);
            Set<Long> userVideoIds = userBehaviors.keySet().stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toSet());

            // 协同过滤：找到与当前用户有相似行为的用户的视频
            Set<Long> recommendedVideoIds = new HashSet<>();
            for (Long videoId : userVideoIds) {
                String videoScoreKey = String.format(VIDEO_SCORE_KEY, videoId);
                Set<Object> similarUsers = redisTemplate.opsForZSet().range(videoScoreKey, 0, -1);
                if (similarUsers != null) {
                    for (Object similarUser : similarUsers) {
                        Long similarUserId = Long.valueOf(similarUser.toString());
                        if (!similarUserId.equals(userId)) {
                            String similarBehaviorKey = String.format(USER_BEHAVIOR_KEY, similarUserId);
                            Map<Object, Object> similarBehaviors = redisTemplate.opsForHash().entries(similarBehaviorKey);
                            similarBehaviors.keySet().stream()
                                    .map(obj -> Long.valueOf(obj.toString()))
                                    .filter(vid -> !userVideoIds.contains(vid))
                                    .forEach(recommendedVideoIds::add);
                        }
                    }
                }
            }

            // 转换为列表并限制大小
            List<Long> result = new ArrayList<>(recommendedVideoIds).subList(0, Math.min(size, recommendedVideoIds.size()));

            // 缓存推荐结果
            if (!result.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(recommendKey, result.toArray());
                redisTemplate.expire(recommendKey, 1, TimeUnit.HOURS);
            }

            logger.info("推荐生成成功：userId={}, size={}", userId, size);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("推荐生成失败：userId={}, size={}, error={}", userId, size, e.getMessage());
            throw new IllegalArgumentException("Failed to generate recommendations: " + e.getMessage(), e);
        }
    }
}
