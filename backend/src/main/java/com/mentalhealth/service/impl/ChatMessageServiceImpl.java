package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.ChatMessageMapper;
import com.mentalhealth.service.ChatMessageService;
import com.mentalhealth.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Resource
    private UserService userService;

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
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(i -> i.eq(ChatMessage::getSenderId, userId1).eq(ChatMessage::getReceiverId, userId2))
                .or(i -> i.eq(ChatMessage::getSenderId, userId2).eq(ChatMessage::getReceiverId, userId1))
                .orderByAsc(ChatMessage::getCreateTime);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        // 把 senderId 发给 receiverId 的所有未读消息标记为已读
        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatMessage::getSenderId, senderId)
                .eq(ChatMessage::getReceiverId, receiverId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1);

        this.update(updateWrapper);
    }

    @Override
    public Map<Long, Integer> getUnreadCounts(Long receiverId) {
        // 查询所有发给当前用户的未读消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getReceiverId, receiverId)
                .eq(ChatMessage::getIsRead, 0);
        List<ChatMessage> unreadMessages = baseMapper.selectList(wrapper);

        // 按发送者分组计数
        Map<Long, Integer> countMap = new HashMap<>();
        for (ChatMessage msg : unreadMessages) {
            countMap.merge(msg.getSenderId(), 1, Integer::sum);
        }
        return countMap;
    }

    @Override
    public List<Map<String, Object>> getConversationList(Long userId) {
        // 找出所有与当前用户相关的消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSenderId, userId)
                .or().eq(ChatMessage::getReceiverId, userId);
        wrapper.orderByDesc(ChatMessage::getCreateTime);
        List<ChatMessage> allMessages = baseMapper.selectList(wrapper);

        // 按对方 ID 分组，取每组最新一条
        Map<Long, ChatMessage> latestMap = new LinkedHashMap<>();
        for (ChatMessage msg : allMessages) {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            latestMap.putIfAbsent(otherUserId, msg);
        }

        // 获取未读数
        Map<Long, Integer> unreadCounts = getUnreadCounts(userId);

        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, ChatMessage> entry : latestMap.entrySet()) {
            Long otherUserId = entry.getKey();
            ChatMessage lastMsg = entry.getValue();

            Map<String, Object> conv = new HashMap<>();
            conv.put("userId", otherUserId);
            conv.put("lastMessage", lastMsg.getContent());
            conv.put("lastMessageTime", lastMsg.getCreateTime());
            conv.put("unreadCount", unreadCounts.getOrDefault(otherUserId, 0));

            // 填充对方用户名和头像
            User otherUser = userService.getById(otherUserId);
            if (otherUser != null) {
                conv.put("username",
                        otherUser.getRealName() != null ? otherUser.getRealName() : otherUser.getUsername());
                conv.put("avatar", otherUser.getAvatar());
            }

            result.add(conv);
        }
        return result;
    }
}
