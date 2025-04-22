package qinghuan.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import qinghuan.api.dto.UserRegisterRequest;
import qinghuan.dao.domain.User;

/**
 * 用户 DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    /**
     * 将注册请求转换为用户实体
     *
     * @param request 注册请求
     * @return 用户实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    User toEntity(UserRegisterRequest request);
}