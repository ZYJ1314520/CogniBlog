package com.example.cogniblog.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long id;
    private String nickname;
    private String avatar;
}
