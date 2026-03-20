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
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getToken(String username, String password) throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("token").asText();
    }

    @Test
    public void testTC21_SubmitAppointment() throws Exception {
        String token = getToken("2021001@edu.cn", "admin123");

        Map<String, Object> appReq = new HashMap<>();
        appReq.put("teacherId", 2L);
        appReq.put("appointDate", "2026-03-20");
        appReq.put("timeSlot", "09:00-10:00");
        appReq.put("requirement", "学业压力大");

        mockMvc.perform(post("/api/appointment/submit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isOk());
    }

    @Test
    public void testTC22_GetMyAppointments() throws Exception {
        String token = getToken("2021001@edu.cn", "admin123");

        mockMvc.perform(get("/api/appointment/my")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testTC23_CancelAppointment() throws Exception {
        String token = getToken("2021001@edu.cn", "admin123");

        mockMvc.perform(post("/api/appointment/1/cancel")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testTC24_GetTeacherAppointments() throws Exception {
        String token = getToken("teacher001", "admin123");

        mockMvc.perform(get("/api/appointment/teacher/list")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testTC25_ProcessAppointment() throws Exception {
        String token = getToken("teacher001", "admin123");

        mockMvc.perform(post("/api/appointment/teacher/process")
                .header("Authorization", "Bearer " + token)
                .param("appointmentId", "1")
                .param("status", "APPROVED")
                .param("feedback", "已安排"))
                .andExpect(status().isOk());
    }
}
