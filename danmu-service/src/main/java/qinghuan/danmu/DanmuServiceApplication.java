package qinghuan.danmu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 弹幕服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("qinghuan.danmu.dao.mapper")
@ComponentScan(basePackages = {"qinghuan.danmu", "qinghuan.common"})
public class DanmuServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DanmuServiceApplication.class, args);
    }
}
