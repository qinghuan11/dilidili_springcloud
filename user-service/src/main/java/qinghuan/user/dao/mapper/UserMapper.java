package qinghuan.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import qinghuan.dao.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 MyBatis 映射器
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}