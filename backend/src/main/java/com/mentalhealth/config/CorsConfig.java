package com.mentalhealth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局 CORS 跨域配置
 * 前后端分离部署场景下，允许前端域名跨域访问后端 API
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端来源（开发环境；生产环境应替换为实际域名）
        config.addAllowedOriginPattern("*");
        // 允许携带 Cookie / Authorization 头
        config.setAllowCredentials(true);
        // 允许的 HTTP 方法
        config.addAllowedMethod("*");
        // 允许的请求头
        config.addAllowedHeader("*");
        // 暴露的响应头（前端可读取）
        config.addExposedHeader("Authorization");
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
