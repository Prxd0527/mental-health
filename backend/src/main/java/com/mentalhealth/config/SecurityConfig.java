package com.mentalhealth.config;

import com.mentalhealth.security.JwtAuthenticationEntryPoint;
import com.mentalhealth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable() // 开启跨域，关闭CSRF
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and() // 未授权处理
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() // 基于JWT无状态
                .authorizeRequests()
                // 放行登录注册相关接口
                .antMatchers("/api/auth/**").permitAll()
                // 放行公开读取类接口（科普文章列表/详情、咨询师列表、问卷列表、树洞广场）
                .antMatchers(
                        "/api/article/list", "/api/article/{id}",
                        "/api/post/square", "/api/post/{id}",
                        "/api/quiz/list", "/api/quiz/{id}/questions",
                        "/api/auth/teachers")
                .permitAll()
                // 放行 WebSocket 握手端点
                .antMatchers("/ws/**").permitAll()
                // 放行上传文件的静态资源访问
                .antMatchers("/uploads/**").permitAll()
                // 放行评论列表公开查看（列表/全部/树形）
                .antMatchers("/api/comment/list/**", "/api/comment/all/**", "/api/comment/tree/**").permitAll()
                // 放行前端静态资源和Swagger文档(视情况拓展)
                .antMatchers(
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/favicon.ico")
                .permitAll()
                // 其他所有请求需要身份认证
                .anyRequest().authenticated();

        // 注入自定义过滤层
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
