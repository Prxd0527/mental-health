package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.ChatMessage;
import com.mentalhealth.service.ChatMessageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private ChatMessageService chatMessageService;

    @GetMapping("/history/{targetUserId}")
    public Result<List<ChatMessage>> getHistory(@PathVariable Long targetUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Result.error(401, "尚未登录");
        }
        Long currentUserId = (Long) auth.getPrincipal();

        List<ChatMessage> history = chatMessageService.getChatHistory(currentUserId, targetUserId);
        return Result.success(history);
    }
}
