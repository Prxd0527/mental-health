package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.mapper.ChatMessageMapper;
import com.mentalhealth.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Override
    public boolean saveMessage(ChatMessage message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(LocalDateTime.now());
        }
        if (message.getIsRead() == null) {
            message.setIsRead(0);
        }
        return this.save(message);
    }

    @Override
    public List<ChatMessage> getChatHistory(Long userId1, Long userId2) {
        // 查找 A 发给 B 或者 B 发给 A 的所有记录
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(i -> i.eq(ChatMessage::getSenderId, userId1).eq(ChatMessage::getReceiverId, userId2))
                .or(i -> i.eq(ChatMessage::getSenderId, userId2).eq(ChatMessage::getReceiverId, userId1))
                .orderByAsc(ChatMessage::getCreateTime);

        return baseMapper.selectList(wrapper);
    }
}
