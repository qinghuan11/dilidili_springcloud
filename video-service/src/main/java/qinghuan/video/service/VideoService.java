package qinghuan.video.service;

import qinghuan.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import qinghuan.dao.domain.Video;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频服务接口
 */
public interface VideoService {

    /**
     * 上传视频
     *
     * @param file        视频文件
     * @param title       标题
     * @param description 描述
     * @param token       JWT 令牌
     * @return 上传结果
     */
    Result<String> uploadVideo(MultipartFile file, String title, String description, String token);

    /**
     * 播放视频
     *
     * @param id 视频ID
     * @return 视频资源
     */
    FileSystemResource playVideo(Long id);

    /**
     * 点赞视频
     *
     * @param id 视频ID
     * @return 点赞结果
     */
    Result<String> likeVideo(Long id);

    /**
     * 查询视频列表
     *
     * @param page   页码
     * @param size   每页大小
     * @param sortBy 排序字段
     * @param order  排序方式
     * @return 视频分页列表
     */
    Result<Page<Video>> listVideos(int page, int size, String sortBy, String order);

    /**
     * 搜索视频
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页大小
     * @param sortBy  排序字段
     * @param order   排序方式
     * @return 视频分页列表
     */
    Result<Page<Video>> searchVideos(String keyword, int page, int size, String sortBy, String order);

    /**
     * 删除视频
     *
     * @param id    视频ID
     * @param token JWT 令牌
     * @return 删除结果
     */
    Result<String> deleteVideo(Long id, String token);

    /**
     * 更新视频
     *
     * @param id          视频ID
     * @param title       标题
     * @param description 描述
     * @param token       JWT 令牌
     * @return 更新结果
     */
    Result<String> updateVideo(Long id, String title, String description, String token);
}