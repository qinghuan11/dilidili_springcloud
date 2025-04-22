package qinghuan.video.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import qinghuan.dao.domain.Video;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频 MyBatis 映射器
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}