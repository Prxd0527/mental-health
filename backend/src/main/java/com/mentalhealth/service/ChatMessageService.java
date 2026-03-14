package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 保存单条聊天记录
     */
    boolean saveMessage(ChatMessage message);

    /**
     * 获取两个用户之间的历史聊天记录
     */
    List<ChatMessage> getChatHistory(Long userId1, Long userId2);

    /**
     * 将 senderId 发给 receiverId 的所有未读消息标记为已读
     */
    void markMessagesAsRead(Long senderId, Long receiverId);

    /**
     * 获取某用户的未读消息计数（按发送者分组）
     *
     * @return Map<发送者ID, 未读数量>
     */
    Map<Long, Integer> getUnreadCounts(Long receiverId);

    /**
     * 获取用户的会话列表（最近联系人 + 最后一条消息 + 未读数）
     */
    List<Map<String, Object>> getConversationList(Long userId);
}
