package qinghuan.ad.service;

import qinghuan.common.Result;
import qinghuan.dao.domain.AdSpace;
import qinghuan.dao.domain.Advertisement;
import org.springframework.web.multipart.MultipartFile;

public interface AdService {
    Result<String> buyAdSpace(Long adSpaceId, Long userId, MultipartFile file, String title);
    Result<String> approveAd(Long adId, String status);
    Result<String> recordImpression(Long adId);
    Result<String> recordClick(Long adId);
    Result<AdSpace> getAdSpace(Long adSpaceId);
    Result<Advertisement> getAdvertisement(Long adId);
}
