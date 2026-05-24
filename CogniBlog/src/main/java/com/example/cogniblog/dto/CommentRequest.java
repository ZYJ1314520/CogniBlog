package com.example.cogniblog.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long id;
    private Long articleId;
    private String content;
    private Long parentId;
}
