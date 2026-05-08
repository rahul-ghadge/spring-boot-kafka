//package com.arya.kafka.controller;
//
//import com.arya.kafka.dto.BatchMessageRequest;
//import com.arya.kafka.dto.NotificationRequest;
//import com.arya.kafka.service.ProducerService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Web-layer slice tests for {@link KafkaController} using Spring's {@code @WebMvcTest}.
// *
// * <p>Only the web layer is loaded; {@link ProducerService} is mocked to isolate
// * controller behaviour from Kafka infrastructure.
// */
//@WebMvcTest(KafkaController.class)
//class KafkaControllerTest {
//
//    @Autowired private MockMvc      mockMvc;
//    @Autowired private ObjectMapper objectMapper;
//
//    @MockitoBean private ProducerService producerService;
//
//    // ─── GET /kafka/publish ───────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("GET /kafka/publish: returns 202 for valid message")
//    void publishMessage_validMessage_returns202() throws Exception {
//        doNothing().when(producerService).sendMessage(anyString());
//
//        mockMvc.perform(get("/kafka/publish").param("message", "hello kafka"))
//                .andExpect(status().isAccepted())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Message published successfully"));
//
//        verify(producerService).sendMessage("hello kafka");
//    }
//
//    @Test
//    @DisplayName("GET /kafka/publish: returns 400 when message param is blank")
//    void publishMessage_blank_returns400() throws Exception {
//        mockMvc.perform(get("/kafka/publish").param("message", "  "))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("GET /kafka/publish: returns 400 when message param is missing")
//    void publishMessage_missing_returns400() throws Exception {
//        mockMvc.perform(get("/kafka/publish"))
//                .andExpect(status().isBadRequest());
//    }
//
//    // ─── POST /kafka/publish ──────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("POST /kafka/publish: returns 202 for valid SuperHero body")
//    void publishSuperHero_valid_returns202() throws Exception {
//        Map<String, Object> body = Map.of(
//                "name",       "Peter Parker",
//                "superName",  "Spider-Man",
//                "profession", "Photographer",
//                "age",        25,
//                "canFly",     false
//        );
//        doNothing().when(producerService).sendSuperHeroMessage(any());
//
//        mockMvc.perform(post("/kafka/publish")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(body)))
//                .andExpect(status().isAccepted())
//                .andExpect(jsonPath("$.success").value(true));
//
//        verify(producerService).sendSuperHeroMessage(any());
//    }
//
//    @Test
//    @DisplayName("POST /kafka/publish: returns 400 when superName is blank")
//    void publishSuperHero_blankSuperName_returns400() throws Exception {
//        Map<String, Object> body = Map.of(
//                "name",       "Peter Parker",
//                "superName",  "",
//                "profession", "Photographer",
//                "age",        25
//        );
//        mockMvc.perform(post("/kafka/publish")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(body)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.data.superName").exists());
//    }
//
//    @Test
//    @DisplayName("POST /kafka/publish: returns 400 when age exceeds maximum")
//    void publishSuperHero_ageTooHigh_returns400() throws Exception {
//        Map<String, Object> body = Map.of(
//                "name",       "Ancient One",
//                "superName",  "The Ancient",
//                "profession", "Sorcerer",
//                "age",        999
//        );
//        mockMvc.perform(post("/kafka/publish")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(body)))
//                .andExpect(status().isBadRequest());
//    }
//
//    // ─── POST /kafka/publish/batch ────────────────────────────────────────────
//
//    @Test
//    @DisplayName("POST /kafka/publish/batch: returns 202 and calls sendMessage per item")
//    void publishBatch_valid_returns202() throws Exception {
//        BatchMessageRequest request = new BatchMessageRequest(List.of("msg1", "msg2", "msg3"));
//        doNothing().when(producerService).sendMessage(anyString());
//
//        mockMvc.perform(post("/kafka/publish/batch")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isAccepted())
//                .andExpect(jsonPath("$.success").value(true));
//
//        verify(producerService, times(3)).sendMessage(anyString());
//    }
//
//    @Test
//    @DisplayName("POST /kafka/publish/batch: returns 400 when messages list is empty")
//    void publishBatch_empty_returns400() throws Exception {
//        BatchMessageRequest request = new BatchMessageRequest(List.of());
//
//        mockMvc.perform(post("/kafka/publish/batch")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    // ─── POST /kafka/notify ────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("POST /kafka/notify: returns 202 for valid notification body")
//    void publishNotification_valid_returns202() throws Exception {
//        NotificationRequest request = new NotificationRequest("system alert: disk at 90%");
//        doNothing().when(producerService).sendNotification(anyString());
//
//        mockMvc.perform(post("/kafka/notify")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isAccepted())
//                .andExpect(jsonPath("$.success").value(true));
//
//        verify(producerService).sendNotification("system alert: disk at 90%");
//    }
//
//    @Test
//    @DisplayName("POST /kafka/notify: returns 400 when message is blank")
//    void publishNotification_blank_returns400() throws Exception {
//        NotificationRequest request = new NotificationRequest("  ");
//
//        mockMvc.perform(post("/kafka/notify")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//}
