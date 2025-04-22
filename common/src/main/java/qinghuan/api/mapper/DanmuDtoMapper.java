package qinghuan.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import qinghuan.dao.domain.Danmu;

/**
 * 弹幕 DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface DanmuDtoMapper {
    /**
     * 将弹幕 DTO 转换为实体
     *
     * @param danmu 弹幕 DTO
     * @return 弹幕实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    Danmu toEntity(Danmu danmu);

    /**
     * 将弹幕实体转换为 DTO
     *
     * @param danmu 弹幕实体
     * @return 弹幕 DTO
     */
    Danmu toDto(Danmu danmu);
}