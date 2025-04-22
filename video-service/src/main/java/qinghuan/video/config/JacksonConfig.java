package qinghuan.video.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置类
 */
@Configuration
public class JacksonConfig {
    private static final Logger logger = LoggerFactory.getLogger(JacksonConfig.class);

    /**
     * 提供 ObjectMapper Bean
     *
     * @return ObjectMapper 实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        logger.debug("创建 ObjectMapper");
        return new ObjectMapper();
    }
}
