CREATE DATABASE IF NOT EXISTS dilidili_ad;
USE dilidili_ad;

-- 广告位表
CREATE TABLE t_ad_space (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL COMMENT '广告位名称',
                            description VARCHAR(255) COMMENT '广告位描述',
                            price DECIMAL(10, 2) NOT NULL COMMENT '广告位价格',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '广告位信息';

-- 广告表
CREATE TABLE t_advertisement (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL COMMENT '广告主用户ID',
                                 ad_space_id BIGINT NOT NULL COMMENT '关联广告位ID',
                                 file_path VARCHAR(255) NOT NULL COMMENT '广告文件路径',
                                 title VARCHAR(100) NOT NULL COMMENT '广告标题',
                                 status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '广告状态',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 FOREIGN KEY (ad_space_id) REFERENCES t_ad_space(id)
) COMMENT '广告信息';

-- 广告性能统计表
CREATE TABLE t_ad_performance (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  ad_id BIGINT NOT NULL COMMENT '广告ID',
                                  impressions BIGINT DEFAULT 0 COMMENT '展示次数',
                                  clicks BIGINT DEFAULT 0 COMMENT '点击次数',
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  FOREIGN KEY (ad_id) REFERENCES t_advertisement(id)
) COMMENT '广告性能统计';