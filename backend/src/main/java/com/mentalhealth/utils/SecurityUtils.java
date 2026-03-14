package com.mentalhealth.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 - 统一获取当前登录用户信息
 * 替代各 Controller 中重复的 getCurrentUserId() 方法
 */
public class SecurityUtils {

    private SecurityUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return userId，未登录返回 null
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户角色（不含 ROLE_ 前缀）
     *
     * @return 角色字符串，如 STUDENT/TEACHER/ADMIN，未登录返回 null
     */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.substring(5))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为教师/咨询师
     */
    public static boolean isTeacher() {
        return "TEACHER".equals(getCurrentUserRole());
    }
}
