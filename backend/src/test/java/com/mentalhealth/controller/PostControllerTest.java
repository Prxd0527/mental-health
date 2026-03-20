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
@Transactional
public class PostControllerTest {

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

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("token").asText();
    }

    @Test
    public void testTC10_PublishPost() throws Exception {
        String token = getStudentToken();

        Map<String, Object> postRequest = new HashMap<>();
        postRequest.put("content", "今天天气真好");
        postRequest.put("tags", "日常");
        postRequest.put("isAnonymous", 1);

        mockMvc.perform(post("/api/post/publish")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testTC11_GetPostSquare() throws Exception {
        mockMvc.perform(get("/api/post/square")
                .param("pageNum", "1")
                .param("pageSize", "5")
                .param("sortBy", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    public void testTC12_GetPostDetail() throws Exception {
        // Assume id=1 exists
        mockMvc.perform(get("/api/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testTC13_LikePost() throws Exception {
        String token = getStudentToken();

        mockMvc.perform(post("/api/post/1/like")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testTC14_GetMyPosts() throws Exception {
        String token = getStudentToken();

        mockMvc.perform(get("/api/post/my")
                .header("Authorization", "Bearer " + token)
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    public void testTC15_DeletePost() throws Exception {
        String token = getStudentToken();

        mockMvc.perform(delete("/api/post/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()); // Typically returns 200, checks may fail if post 1 is not owned by student 1
    }
}
