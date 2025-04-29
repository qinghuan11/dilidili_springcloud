package qinghuan.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_advertisement")
public class Advertisement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long adSpaceId;
    private String filePath;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
