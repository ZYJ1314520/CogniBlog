package com.example.cogniblog.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleRequest {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private List<Long> tagIds;
}
