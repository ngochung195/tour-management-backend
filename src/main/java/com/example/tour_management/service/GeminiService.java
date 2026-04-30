package com.example.tour_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new RuntimeException("GEMINI_API_KEY is missing!");
        }
    }

    public String call(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map body = response.getBody();
            if (body == null) return "Không có phản hồi từ Gemini";

            List<Map> candidates = (List<Map>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "Gemini không trả về kết quả";

            Map contentRes = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) contentRes.get("parts");
            if (parts == null || parts.isEmpty()) return "Không có nội dung";

            return parts.get(0).get("text").toString();

        } catch (Exception e) {
            return "Lỗi khi gọi Gemini: " + e.getMessage();
        }
    }
}