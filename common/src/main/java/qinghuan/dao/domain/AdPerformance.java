package qinghuan.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ad_performance")
public class AdPerformance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adId;
    private Long impressions;
    private Long clicks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
