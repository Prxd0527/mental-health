package com.mentalhealth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 头像已改为 Base64 存储在数据库中，不再需要磁盘静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // 如需其他静态资源映射可在此添加
}
