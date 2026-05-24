package com.example.cogniblog.controller;

import com.example.cogniblog.common.BusinessException;
import com.example.cogniblog.common.Result;
import com.example.cogniblog.entity.Tag;
import com.example.cogniblog.service.TagService;
import com.example.cogniblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final UserService userService;

    @GetMapping("/list")
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return Result.success(tags);
    }

    @GetMapping("/{id}")
    public Result<Tag> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        return Result.success(tag);
    }

    @PostMapping("/add")
    public Result<Void> addTag(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Tag tag) {
        getUserIdAndCheckAdmin(token);
        tagService.addTag(tag);
        return Result.success("添加成功", null);
    }

    @PutMapping("/update")
    public Result<Void> updateTag(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Tag tag) {
        getUserIdAndCheckAdmin(token);
        tagService.updateTag(tag);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteTag(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        getUserIdAndCheckAdmin(token);
        tagService.deleteTag(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/bind")
    public Result<Void> bindTags(@RequestParam Long articleId, @RequestBody List<Long> tagIds) {
        tagService.bindTags(articleId, tagIds);
        return Result.success("绑定成功", null);
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
