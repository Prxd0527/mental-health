package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.service.ChatMessageService;
import com.mentalhealth.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private ChatMessageService chatMessageService;

    /**
     * 获取与指定用户的聊天历史
     */
    @GetMapping("/history/{targetUserId}")
    public Result<List<ChatMessage>> getHistory(@PathVariable Long targetUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null)
            return Result.error(401, "尚未登录");

        List<ChatMessage> history = chatMessageService.getChatHistory(currentUserId, targetUserId);
        return Result.success(history);
    }

    /**
     * 标记与某用户的消息为已读（批量：将对方发给我的所有未读消息置为已读）
     */
    @PostMapping("/read/{senderId}")
    public Result<String> markAsRead(@PathVariable Long senderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null)
            return Result.error(401, "尚未登录");

        chatMessageService.markMessagesAsRead(senderId, currentUserId);
        return Result.success("消息已标记为已读");
    }

    /**
     * 获取当前用户的未读消息数量统计（按发送者分组）
     */
    @GetMapping("/unread")
    public Result<Map<Long, Integer>> getUnreadCounts() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null)
            return Result.error(401, "尚未登录");

        Map<Long, Integer> unreadMap = chatMessageService.getUnreadCounts(currentUserId);
        return Result.success(unreadMap);
    }

    /**
     * 获取当前用户的会话列表（最近联系人）
     */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null)
            return Result.error(401, "尚未登录");

        List<Map<String, Object>> conversations = chatMessageService.getConversationList(currentUserId);
        return Result.success(conversations);
    }
}
