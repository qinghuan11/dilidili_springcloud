package qinghuan.ad.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import qinghuan.ad.service.AdService;
import qinghuan.common.JwtUtil;
import qinghuan.common.Result;
import qinghuan.dao.domain.AdSpace;
import qinghuan.dao.domain.Advertisement;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdController {
    private static final Logger logger = LoggerFactory.getLogger(AdController.class);

    private final AdService adService;
    private final JwtUtil jwtUtil;

    @PostMapping("/buy")
    public ResponseEntity<Result<String>> buyAdSpace(
            @RequestParam Long adSpaceId,
            @RequestPart MultipartFile file,
            @RequestPart @NotBlank(message = "Title cannot be empty") String title,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("处理广告位购买请求：adSpaceId={}, userId={}", adSpaceId, userId);
        Result<String> result = adService.buyAdSpace(adSpaceId, userId, file, title);
        logger.debug("广告位购买成功：adSpaceId={}", adSpaceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{adId}/approve")
    public ResponseEntity<Result<String>> approveAd(
            @PathVariable Long adId,
            @RequestParam String status) {
        logger.info("处理广告审批请求：adId={}, status={}", adId, status);
        Result<String> result = adService.approveAd(adId, status);
        logger.debug("广告审批成功：adId={}", adId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{adId}/impression")
    public ResponseEntity<Result<String>> recordImpression(@PathVariable Long adId) {
        logger.info("处理广告展示记录请求：adId={}", adId);
        Result<String> result = adService.recordImpression(adId);
        logger.debug("广告展示记录成功：adId={}", adId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{adId}/click")
    public ResponseEntity<Result<String>> recordClick(@PathVariable Long adId) {
        logger.info("处理广告点击记录请求：adId={}", adId);
        Result<String> result = adService.recordClick(adId);
        logger.debug("广告点击记录成功：adId={}", adId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/space/{adSpaceId}")
    public ResponseEntity<Result<AdSpace>> getAdSpace(@PathVariable Long adSpaceId) {
        logger.info("处理广告位查询请求：adSpaceId={}", adSpaceId);
        Result<AdSpace> result = adService.getAdSpace(adSpaceId);
        logger.debug("广告位查询成功：adSpaceId={}", adSpaceId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{adId}")
    public ResponseEntity<Result<Advertisement>> getAdvertisement(@PathVariable Long adId) {
        logger.info("处理广告查询请求：adId={}", adId);
        Result<Advertisement> result = adService.getAdvertisement(adId);
        logger.debug("广告查询成功：adId={}", adId);
        return ResponseEntity.ok(result);
    }
}
