package qinghuan.danmu.controller;

import qinghuan.dao.domain.Danmu;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 弹幕 WebSocket 处理类
 */
@Component
public class DanmuWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DanmuWebSocketHandler.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        logger.info("WebSocket 连接已建立：sessionId={}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        logger.info("WebSocket 连接已关闭：sessionId={}, status={}", session.getId(), status);
    }

    /**
     * 广播弹幕消息给所有连接的客户端
     *
     * @param danmu 弹幕对象
     */
    public void broadcastDanmu(Danmu danmu) {
        try {
            String message = objectMapper.writeValueAsString(danmu);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
            logger.debug("弹幕已广播：danmuId={}", danmu.getId());
        } catch (Exception e) {
            logger.error("弹幕广播失败：error={}", e.getMessage());
        }
    }
}