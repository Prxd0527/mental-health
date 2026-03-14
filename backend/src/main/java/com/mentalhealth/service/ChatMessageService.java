package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 保存单条聊天记录
     */
    boolean saveMessage(ChatMessage message);

    /**
     * 获取两个用户之间的历史聊天记录
     *
     * @param userId1 第一个用户ID
     * @param userId2 第二个用户ID
     * @return 历史消息列表（按时间升序）
     */
    List<ChatMessage> getChatHistory(Long userId1, Long userId2);
}
