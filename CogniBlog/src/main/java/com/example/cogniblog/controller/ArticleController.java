package com.example.cogniblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cogniblog.common.Result;
import com.example.cogniblog.dto.ArticleRequest;
import com.example.cogniblog.entity.Article;
import com.example.cogniblog.service.ArticleService;
import com.example.cogniblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;

    @GetMapping("/list")
    public Result<Page<Article>> getArticlePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword) {
        Page<Article> page = articleService.getArticlePage(current, size, categoryId, tagId, userId, keyword);
        return Result.success(page);
    }

    @GetMapping("/my")
    public Result<Page<Article>> getMyArticles(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        if (token == null || token.isEmpty()) {
            return Result.error(401, "未登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = userService.getUserIdByToken(token);
        if (userId == null) {
            return Result.error(401, "Token 无效");
        }
        Page<Article> page = articleService.getArticlesByUserId(userId, current, size);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Article> getArticleDetail(@PathVariable Long id) {
        Article article = articleService.getArticleDetail(id);
        return Result.success(article);
    }

    @PostMapping("/add")
    public Result<Void> addArticle(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody ArticleRequest request) {
        if (token == null || token.isEmpty()) {
            return Result.error(401, "未登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = userService.getUserIdByToken(token);
        if (userId == null) {
            return Result.error(401, "Token 无效");
        }
        articleService.addArticle(request, userId);
        return Result.success("发布成功", null);
    }

    @PutMapping("/update")
    public Result<Void> updateArticle(@RequestBody ArticleRequest request) {
        articleService.updateArticle(request);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success("删除成功", null);
    }
}
