package qinghuan.danmu.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import qinghuan.dao.domain.Danmu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 弹幕 MyBatis 映射器
 */
@Mapper
public interface DanmuMapper extends BaseMapper<Danmu> {
}
