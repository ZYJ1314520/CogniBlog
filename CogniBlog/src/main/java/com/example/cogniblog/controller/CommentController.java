package com.example.cogniblog.controller;

import com.example.cogniblog.common.Result;
import com.example.cogniblog.dto.CommentRequest;
import com.example.cogniblog.entity.Comment;
import com.example.cogniblog.service.CommentService;
import com.example.cogniblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @GetMapping("/list")
    public Result<List<Comment>> getCommentsByArticleId(@RequestParam Long articleId) {
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        return Result.success(comments);
    }

    @PostMapping("/add")
    public Result<Void> addComment(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody CommentRequest request) {
        if (token == null || token.isEmpty()) {
            return Result.error(401, "请先登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = userService.getUserIdByToken(token);
        if (userId == null) {
            return Result.error(401, "Token 无效");
        }
        commentService.addComment(request, userId);
        return Result.success("评论成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteComment(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        if (token == null || token.isEmpty()) {
            return Result.error(401, "请先登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = userService.getUserIdByToken(token);
        if (userId == null) {
            return Result.error(401, "Token 无效");
        }
        commentService.deleteComment(id, userId);
        return Result.success("删除成功", null);
    }
}
