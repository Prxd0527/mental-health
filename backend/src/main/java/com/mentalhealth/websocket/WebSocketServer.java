package com.mentalhealth.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/chat/{userId}")
@Component
@Slf4j
public class WebSocketServer {

    // 静态变量，用来记录当前在线连接数。(其实可以用 sessionMap.size())
    private static final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    // Spring Bean 注入在 WebSocket 中比较特殊，必须通过静态或者 Setter 方式
    private static ChatMessageService chatMessageService;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        WebSocketServer.chatMessageService = chatMessageService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        sessionMap.put(userId, session);
        log.info("用户[{}]已连接，当前在线人数: {}", userId, sessionMap.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        sessionMap.remove(userId);
        log.info("用户[{}]已断开，当前在线人数: {}", userId, sessionMap.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * 客户端发送的格式需为 JSON 序列化的 ChatMessage 对象
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            chatMessage.setSenderId(userId);

            // 1. 持久化到数据库
            if (chatMessageService != null) {
                chatMessageService.saveMessage(chatMessage);
            }

            // 2. 推送给目标用户（如果不在线则只保留在数据库）
            Long receiverId = chatMessage.getReceiverId();
            Session receiverSession = sessionMap.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                // 将完整消息（含ID和时间）回传给接收方
                receiverSession.getBasicRemote().sendText(objectMapper.writeValueAsString(chatMessage));
            } else {
                log.info("用户[{}]不在线，消息已转存为离线消息", receiverId);
            }
        } catch (Exception e) {
            log.error("解析消息或转发异常", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误", error);
    }
}
