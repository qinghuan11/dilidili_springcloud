package qinghuan.like.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import qinghuan.common.Result;
import qinghuan.dao.domain.Collect;

public interface LikeCollectService {
    Result<String> likeVideo(Long userId, Long videoId);
    Result<String> unlikeVideo(Long userId, Long videoId);
    Result<String> collectVideo(Long userId, Long videoId);
    Result<String> uncollectVideo(Long userId, Long videoId);
    Result<Page<Collect>> listCollections(Long userId, int page, int size);
    Result<Long> getLikeCount(Long videoId);
    Result<Long> getCollectCount(Long videoId);
}
