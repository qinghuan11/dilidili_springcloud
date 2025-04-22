package qinghuan.api.mapper;

import org.mapstruct.Mapper;
import qinghuan.dao.domain.Video;

/**
 * 视频 DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface VideoDtoMapper {
    /**
     * 将视频实体转换为 DTO（占位，当前无 DTO 定义）
     *
     * @param video 视频实体
     * @return 视频 DTO
     */
    Video toDto(Video video);

    /**
     * 将 DTO 转换为视频实体（占位，当前无 DTO 定义）
     *
     * @param video 视频 DTO
     * @return 视频实体
     */
    Video toEntity(Video video);
}