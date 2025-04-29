CREATE DATABASE IF NOT EXISTS dilidili_like_collect;
USE dilidili_like_collect;

-- 点赞表
CREATE TABLE t_like (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL COMMENT '用户ID',
                        video_id BIGINT NOT NULL COMMENT '视频ID',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_user_video (user_id, video_id)
) COMMENT '视频点赞记录';

-- 收藏表
CREATE TABLE t_collect (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL COMMENT '用户ID',
                           video_id BIGINT NOT NULL COMMENT '视频ID',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE KEY uk_user_video (user_id, video_id)
) COMMENT '视频收藏记录';