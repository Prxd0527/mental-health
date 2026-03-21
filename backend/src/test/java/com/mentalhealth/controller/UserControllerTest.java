package com.mentalhealth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Automatically roll back database transactions after each test
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to login and get token
    private String getStudentToken() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "2021001@edu.cn");
        loginRequest.put("password", "admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract token from response JSON
        String responseContent = result.getResponse().getContentAsString();
        // A simple way to extract token string (in real code, parsing JSON node is safer)
        return objectMapper.readTree(responseContent).get("data").get("token").asText();
    }

    @Test
    public void testTC01_RegisterUser() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "test2026@edu.cn");
        registerRequest.put("password", "test123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("test2026@edu.cn"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    public void testTC02_TC03_LoginUser() throws Exception {
        // Success
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "2021001@edu.cn");
        loginRequest.put("password", "admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").isNotEmpty());

        // Failure
        loginRequest.put("password", "wrongpwd");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)); // Depending on GlobalExceptionHandler
    }

    @Test
    public void testTC04_GetTeachers() throws Exception {
        mockMvc.perform(get("/api/auth/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
    }

    @Test
    public void testTC05_GetProfile() throws Exception {
        String token = getStudentToken();

        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("2021001@edu.cn"));
    }

    @Test
    public void testTC06_ChangePassword() throws Exception {
        String token = getStudentToken();

        Map<String, String> pwdRequest = new HashMap<>();
        pwdRequest.put("oldPassword", "admin123");
        pwdRequest.put("newPassword", "654321");

        mockMvc.perform(put("/api/user/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pwdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify login with new password
        Map<String, String> newLoginRequest = new HashMap<>();
        newLoginRequest.put("username", "2021001@edu.cn");
        newLoginRequest.put("password", "654321");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testTC07_UpdateProfile() throws Exception {
        String token = getStudentToken();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("realName", "测试用户更新");
        updateRequest.put("gender", 1);

        mockMvc.perform(put("/api/user/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify update
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.realName").value("测试用户更新"))
                .andExpect(jsonPath("$.data.gender").value(1));
    }
}
