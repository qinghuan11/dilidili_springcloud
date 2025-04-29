package qinghuan.like;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("qinghuan.like.dao.mapper")
public class LikeCollectServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LikeCollectServiceApplication.class, args);
    }
}
