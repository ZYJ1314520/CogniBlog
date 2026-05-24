package com.example.cogniblog.service.impl;

import com.example.cogniblog.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.api-key}")
    private String apiKey;

    private static final String MODEL = "deepseek-chat";

    @Override
    public String generateSummary(String content) {
        String prompt = "请为以下文章生成一段简短的摘要（不超过100字），概括文章的核心内容：\n\n" + content;
        return callDeepSeek(prompt);
    }

    @Override
    public List<String> generateTags(String title, String content) {
        String prompt = "请根据以下文章内容，推荐3-5个相关标签（只返回标签名称，用逗号分隔，不要包含其他内容）：\n\n标题：" + title + "\n内容：" + content;
        String response = callDeepSeek(prompt);
        return parseTags(response);
    }

    @Override
    public String rewriteContent(String content) {
        String prompt = "请对以下文章内容进行润色和优化，改善语句通顺性和表达清晰度：\n\n" + content;
//        （只返回润色后的内容，不要包含其他解释）
        return callDeepSeek(prompt);
    }

    @Override
    public String chat(String message) {
        String prompt = "你是一个博客网站的AI助手，请回答用户的问题。如果问题与博客网站使用相关，请提供帮助；如果问题超出范围，请礼貌地说明你只能回答与博客相关的问题。\n\n用户问题：" + message;
        return callDeepSeek(prompt);
    }

    private String callDeepSeek(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 500,
                "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            return "AI 服务调用失败：" + e.getMessage();
        }
    }

    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                if (!content.isEmpty()) {
                    return content;
                }
            }
            return "AI 响应格式错误";
        } catch (Exception e) {
            log.error("解析 AI 响应失败", e);
            return "AI 响应解析失败";
        }
    }

    private List<String> parseTags(String response) {
        List<String> tags = new ArrayList<>();
        String[] parts = response.split("[，,]");
        for (String part : parts) {
            String tag = part.trim().replaceAll("[\"\\s]", "");
            if (!tag.isEmpty() && tag.length() <= 20) {
                tags.add(tag);
            }
        }
        return tags.isEmpty() ? List.of("技术", "编程") : tags;
    }
}