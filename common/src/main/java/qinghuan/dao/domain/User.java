package qinghuan.dao.domain;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 */
@Data
@TableName("t_user")
public class User {
    /** 用户ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，非空 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码，非空 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 邮箱，需符合邮箱格式 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}