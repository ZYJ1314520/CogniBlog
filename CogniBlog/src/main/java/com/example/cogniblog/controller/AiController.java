package com.example.cogniblog.controller;

import com.example.cogniblog.common.Result;
import com.example.cogniblog.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/summary")
    public Result<String> generateSummary(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.isEmpty()) {
            return Result.error("文章内容不能为空");
        }
        String summary = aiService.generateSummary(content);
        return Result.success(summary);
    }

    @PostMapping("/tag")
    public Result<List<String>> generateTags(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");
        if (title == null || content == null || title.isEmpty() || content.isEmpty()) {
            return Result.error("标题和内容不能为空");
        }
        List<String> tags = aiService.generateTags(title, content);
        return Result.success(tags);
    }

    @PostMapping("/rewrite")
    public Result<String> rewriteContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.isEmpty()) {
            return Result.error("文章内容不能为空");
        }
        String rewritten = aiService.rewriteContent(content);
        return Result.success(rewritten);
    }

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        String response = aiService.chat(message);
        return Result.success(response);
    }
}