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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getStudentToken() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "2021001@edu.cn");
        loginRequest.put("password", "admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("token").asText();
    }

    @Test
    public void testTC16_PublishComment() throws Exception {
        String token = getStudentToken();

        Map<String, Object> commentReq = new HashMap<>();
        commentReq.put("postId", 1L);
        commentReq.put("content", "加油！");
        commentReq.put("isAnonymous", 0);

        mockMvc.perform(post("/api/comment/publish")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testTC17_GetCommentList() throws Exception {
        mockMvc.perform(get("/api/comment/list/1")
                .param("pageNum", "1")
                .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    public void testTC18_GetAllComments() throws Exception {
        mockMvc.perform(get("/api/comment/all/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testTC19_GetCommentsTree() throws Exception {
        mockMvc.perform(get("/api/comment/tree/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testTC20_LikeComment() throws Exception {
        String token = getStudentToken();

        mockMvc.perform(post("/api/comment/1/like")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
