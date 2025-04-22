package qinghuan.dao.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 弹幕实体类
 */
@Data
@TableName("t_danmu")
public class Danmu {
    /** 弹幕ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 视频ID */
    private Long videoId;

    /** 用户ID */
    private Long userId;

    /** 弹幕内容，非空 */
    @NotBlank(message = "弹幕内容不能为空")
    private String content;

    /** 弹幕出现时间戳（视频时间轴，单位秒） */
    private Float timestamp;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}