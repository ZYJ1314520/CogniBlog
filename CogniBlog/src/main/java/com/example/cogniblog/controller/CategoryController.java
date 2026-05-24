package com.example.cogniblog.controller;

import com.example.cogniblog.common.BusinessException;
import com.example.cogniblog.common.Result;
import com.example.cogniblog.entity.Article;
import com.example.cogniblog.entity.Category;
import com.example.cogniblog.service.CategoryService;
import com.example.cogniblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping("/list")
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @PostMapping("/add")
    public Result<Void> addCategory(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Category category) {
        Long userId = getUserIdAndCheckAdmin(token);
        categoryService.addCategory(category);
        return Result.success("添加成功", null);
    }

    @PutMapping("/update")
    public Result<Void> updateCategory(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Category category) {
        getUserIdAndCheckAdmin(token);
        categoryService.updateCategory(category);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCategory(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        getUserIdAndCheckAdmin(token);
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/articles/{id}")
    public Result<List<Article>> getArticlesByCategory(@PathVariable Long id) {
        List<Article> articles = categoryService.getArticlesByCategory(id);
        return Result.success(articles);
    }

    private Long getUserIdAndCheckAdmin(String token) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = userService.getUserIdByToken(token);
        if (userId == null) {
            throw new BusinessException(401, "Token 无效");
        }
        if (!userService.isAdmin(userId)) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        return userId;
    }
}
