package qinghuan.ad.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import qinghuan.common.JwtUtil;
import qinghuan.common.Result;
import qinghuan.dao.domain.AdSpace;
import qinghuan.dao.domain.Advertisement;
import qinghuan.dao.domain.AdPerformance;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import qinghuan.ad.dao.mapper.AdSpaceMapper;
import qinghuan.ad.dao.mapper.AdvertisementMapper;
import qinghuan.ad.dao.mapper.AdPerformanceMapper;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AdServiceImpl extends ServiceImpl<AdvertisementMapper, Advertisement> implements AdService {
    private static final Logger logger = LoggerFactory.getLogger(AdServiceImpl.class);
    private static final String AD_CACHE_KEY_PREFIX = "ad:id:";

    private final AdSpaceMapper adSpaceMapper;
    private final AdvertisementMapper advertisementMapper;
    private final AdPerformanceMapper adPerformanceMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MinioClient minioClient;
    private final JwtUtil jwtUtil;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public Result<String> buyAdSpace(Long adSpaceId, Long userId, MultipartFile file, String title) {
        try {
            AdSpace adSpace = adSpaceMapper.selectById(adSpaceId);
            if (adSpace == null) {
                logger.warn("广告位购买失败：广告位不存在，adSpaceId={}", adSpaceId);
                throw new IllegalArgumentException("Ad space not found");
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            Advertisement ad = new Advertisement();
            ad.setUserId(userId);
            ad.setAdSpaceId(adSpaceId);
            ad.setFilePath(fileName);
            ad.setTitle(title);
            ad.setStatus("PENDING");
            advertisementMapper.insert(ad);

            AdPerformance performance = new AdPerformance();
            performance.setAdId(ad.getId());
            performance.setImpressions(0L);
            performance.setClicks(0L);
            adPerformanceMapper.insert(performance);

            logger.info("广告位购买成功：adSpaceId={}, userId={}", adSpaceId, userId);
            return Result.success("Ad space purchased successfully");
        } catch (Exception e) {
            logger.error("广告位购买失败：adSpaceId={}, userId={}, error={}", adSpaceId, userId, e.getMessage());
            throw new IllegalArgumentException("Failed to buy ad space: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> approveAd(Long adId, String status) {
        try {
            Advertisement ad = advertisementMapper.selectById(adId);
            if (ad == null) {
                logger.warn("广告审批失败：广告不存在，adId={}", adId);
                throw new IllegalArgumentException("Advertisement not found");
            }
            if (!"APPROVED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
                logger.warn("广告审批失败：状态无效，adId={}, status={}", adId, status);
                throw new IllegalArgumentException("Invalid status");
            }
            ad.setStatus(status);
            advertisementMapper.updateById(ad);
            evictAdCache(adId);
            logger.info("广告审批成功：adId={}, status={}", adId, status);
            return Result.success("Advertisement " + status.toLowerCase() + " successfully");
        } catch (Exception e) {
            logger.error("广告审批失败：adId={}, error={}", adId, e.getMessage());
            throw new IllegalArgumentException("Failed to approve ad: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> recordImpression(Long adId) {
        try {
            AdPerformance performance = adPerformanceMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AdPerformance>().eq("ad_id", adId));
            if (performance == null) {
                logger.warn("记录展示失败：广告性能记录不存在，adId={}", adId);
                throw new IllegalArgumentException("Ad performance not found");
            }
            performance.setImpressions(performance.getImpressions() + 1);
            adPerformanceMapper.updateById(performance);
            logger.info("广告展示记录成功：adId={}", adId);
            return Result.success("Impression recorded successfully");
        } catch (Exception e) {
            logger.error("记录展示失败：adId={}, error={}", adId, e.getMessage());
            throw new IllegalArgumentException("Failed to record impression: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> recordClick(Long adId) {
        try {
            AdPerformance performance = adPerformanceMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AdPerformance>().eq("ad_id", adId));
            if (performance == null) {
                logger.warn("记录点击失败：广告性能记录不存在，adId={}", adId);
                throw new IllegalArgumentException("Ad performance not found");
            }
            performance.setClicks(performance.getClicks() + 1);
            adPerformanceMapper.updateById(performance);
            logger.info("广告点击记录成功：adId={}", adId);
            return Result.success("Click recorded successfully");
        } catch (Exception e) {
            logger.error("记录点击失败：adId={}, error={}", adId, e.getMessage());
            throw new IllegalArgumentException("Failed to record click: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<AdSpace> getAdSpace(Long adSpaceId) {
        try {
            AdSpace adSpace = adSpaceMapper.selectById(adSpaceId);
            if (adSpace == null) {
                logger.warn("广告位查询失败：广告位不存在，adSpaceId={}", adSpaceId);
                throw new IllegalArgumentException("Ad space not found");
            }
            logger.info("广告位查询成功：adSpaceId={}", adSpaceId);
            return Result.success(adSpace);
        } catch (Exception e) {
            logger.error("广告位查询失败：adSpaceId={}, error={}", adSpaceId, e.getMessage());
            throw new IllegalArgumentException("Failed to get ad space: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<Advertisement> getAdvertisement(Long adId) {
        try {
            String cacheKey = AD_CACHE_KEY_PREFIX + adId;
            Advertisement cachedAd = (Advertisement) redisTemplate.opsForValue().get(cacheKey);
            if (cachedAd != null) {
                logger.debug("从缓存获取广告：adId={}", adId);
                return Result.success(cachedAd);
            }
            Advertisement ad = advertisementMapper.selectById(adId);
            if (ad == null) {
                logger.warn("广告查询失败：广告不存在，adId={}", adId);
                throw new IllegalArgumentException("Advertisement not found");
            }
            cacheAd(ad);
            logger.info("广告查询成功：adId={}", adId);
            return Result.success(ad);
        } catch (Exception e) {
            logger.error("广告查询失败：adId={}, error={}", adId, e.getMessage());
            throw new IllegalArgumentException("Failed to get advertisement: " + e.getMessage(), e);
        }
    }

    private void cacheAd(Advertisement ad) {
        String cacheKey = AD_CACHE_KEY_PREFIX + ad.getId();
        try {
            redisTemplate.opsForValue().set(cacheKey, ad, 1, TimeUnit.HOURS);
            logger.debug("广告已缓存：id={}", ad.getId());
        } catch (Exception e) {
            logger.error("缓存广告失败：id={}, error={}", ad.getId(), e.getMessage());
        }
    }

    private void evictAdCache(Long adId) {
        String cacheKey = AD_CACHE_KEY_PREFIX + adId;
        try {
            redisTemplate.delete(cacheKey);
            logger.debug("广告缓存已清除：id={}", adId);
        } catch (Exception e) {
            logger.error("清除广告缓存失败：id={}, error={}", adId, e.getMessage());
        }
    }
}
