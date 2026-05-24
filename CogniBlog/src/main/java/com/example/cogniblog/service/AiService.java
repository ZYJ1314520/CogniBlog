package com.example.cogniblog.service;

import java.util.List;

public interface AiService {

    /**
     * 生成文章摘要
     */
    String generateSummary(String content);

    /**
     * 生成文章标签
     */
    List<String> generateTags(String title, String content);

    /**
     * 内容润色
     */
    String rewriteContent(String content);

    /**
     * AI 问答
     */
    String chat(String message);
}