package com.example.cogniblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cogniblog.common.BusinessException;
import com.example.cogniblog.dto.CommentRequest;
import com.example.cogniblog.entity.Article;
import com.example.cogniblog.entity.Comment;
import com.example.cogniblog.entity.User;
import com.example.cogniblog.mapper.ArticleMapper;
import com.example.cogniblog.mapper.CommentMapper;
import com.example.cogniblog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;

    public List<Comment> getCommentsByArticleId(Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId);
        wrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> allComments = commentMapper.selectList(wrapper);

        // 填充用户信息并构建树形结构
        for (Comment comment : allComments) {
            fillCommentUser(comment);
        }

        // 构建楼中楼结构
        return buildCommentTree(allComments);
    }

    public void addComment(CommentRequest request, Long userId) {
        // 检查文章是否存在
        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }

        // 如果有父评论，检查父评论是否存在
        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException(404, "父评论不存在");
            }
            // 确保父评论属于同一篇文章
            if (!parent.getArticleId().equals(request.getArticleId())) {
                throw new BusinessException(400, "父评论不属于该文章");
            }
        }

        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        commentMapper.insert(comment);
    }

    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }

        // 检查是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的评论");
        }

        // 删除该评论的所有子评论（楼中楼）
        deleteCommentWithChildren(id);

        // 删除评论本身
        commentMapper.deleteById(id);
    }

    private void deleteCommentWithChildren(Long parentId) {
        // 查找所有子评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId);
        List<Comment> children = commentMapper.selectList(wrapper);

        // 递归删除子评论
        for (Comment child : children) {
            deleteCommentWithChildren(child.getId());
            commentMapper.deleteById(child.getId());
        }
    }

    private void fillCommentUser(Comment comment) {
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            user.setPassword(null);
            comment.setUser(user);
        }
    }

    private List<Comment> buildCommentTree(List<Comment> allComments) {
        // 分离父评论和子评论
        List<Comment> parentComments = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        for (Comment parent : parentComments) {
            parent.setChildren(findChildren(parent.getId(), allComments));
        }

        return parentComments;
    }

    private List<Comment> findChildren(Long parentId, List<Comment> allComments) {
        return allComments.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .peek(c -> c.setChildren(findChildren(c.getId(), allComments)))
                .collect(Collectors.toList());
    }
}
