package qinghuan.auth;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * 认证服务启动类
 */
@SpringBootApplication
//@EnableDiscoveryClient
@MapperScan("qinghuan.auth.dao.mapper")
//@ComponentScan(basePackages = {"qinghuan.auth", "qinghuan.common"})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
