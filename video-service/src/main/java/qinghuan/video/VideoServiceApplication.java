package qinghuan.video;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 视频服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("qinghuan.video.dao.mapper")
@ComponentScan(basePackages = {"qinghuan.video", "qinghuan.common"})
public class VideoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class, args);
    }
}