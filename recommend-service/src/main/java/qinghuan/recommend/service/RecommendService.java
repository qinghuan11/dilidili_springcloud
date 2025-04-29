package qinghuan.recommend.service;

import qinghuan.common.Result;

import java.util.List;

public interface RecommendService {
    Result<String> recordBehavior(Long userId, Long videoId, String action);
    Result<List<Long>> getRecommendations(Long userId, int size);
}
