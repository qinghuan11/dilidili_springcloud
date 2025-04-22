package qinghuan.dao.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 视频实体类
 */
@Data
@TableName("t_video")
public class Video {
    /** 视频ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题，非空 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 描述 */
    private String description;

    /** 文件路径，非空 */
    @NotBlank(message = "文件路径不能为空")
    private String filePath;

    /** 上传用户ID */
    private Long userId;

    /** 播放量 */
    private Long viewCount;

    /** 点赞数 */
    private Long likeCount;

    /** 上传时间 */
    private Date uploadTime;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}