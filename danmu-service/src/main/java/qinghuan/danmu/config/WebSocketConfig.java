package qinghuan.danmu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import qinghuan.danmu.controller.DanmuWebSocketHandler;

/**
 * WebSocket 配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DanmuWebSocketHandler danmuWebSocketHandler;

    public WebSocketConfig(DanmuWebSocketHandler danmuWebSocketHandler) {
        this.danmuWebSocketHandler = danmuWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(danmuWebSocketHandler, "/danmu").setAllowedOrigins("*");
    }
}