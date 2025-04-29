package qinghuan.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"qinghuan.recommend", "qinghuan.common"})
public class RecommendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendServiceApplication.class, args);
    }
}
