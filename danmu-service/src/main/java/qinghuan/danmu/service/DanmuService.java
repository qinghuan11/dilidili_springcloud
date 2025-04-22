package qinghuan.danmu.service;

import qinghuan.common.Result;
import qinghuan.dao.domain.Danmu;

import java.util.List;

/**
 * 弹幕服务接口
 */
public interface DanmuService {

    /**
     * 发送弹幕
     *
     * @param videoId 视频ID
     * @param content 弹幕内容
     * @param token   JWT 令牌
     * @return 发送结果
     */
    Result<String> sendDanmu(Long videoId, String content, String token);

    /**
     * 获取视频的弹幕列表
     *
     * @param videoId 视频ID
     * @return 弹幕列表
     */
    Result<List<Danmu>> listDanmu(Long videoId);
}
