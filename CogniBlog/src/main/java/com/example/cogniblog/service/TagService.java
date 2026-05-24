package com.example.cogniblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cogniblog.common.BusinessException;
import com.example.cogniblog.entity.ArticleTag;
import com.example.cogniblog.entity.Tag;
import com.example.cogniblog.mapper.ArticleTagMapper;
import com.example.cogniblog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Cacheable(value = "tagList", key = "'all'")
    public List<Tag> getAllTags() {
        return tagMapper.selectList(null);
    }

    public Tag getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(404, "标签不存在");
        }
        return tag;
    }

    @CacheEvict(value = "tagList", allEntries = true)
    public void addTag(Tag tag) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, tag.getName());
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "标签名称已存在");
        }
        tagMapper.insert(tag);
    }

    @CacheEvict(value = "tagList", allEntries = true)
    public void updateTag(Tag tag) {
        Tag existing = tagMapper.selectById(tag.getId());
        if (existing == null) {
            throw new BusinessException(404, "标签不存在");
        }
        tagMapper.updateById(tag);
    }

    @CacheEvict(value = "tagList", allEntries = true)
    public void deleteTag(Long id) {
        // 删除标签与文章的关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getTagId, id);
        articleTagMapper.delete(wrapper);

        // 删除标签
        tagMapper.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = "tagList", allEntries = true)
    public void bindTags(Long articleId, List<Long> tagIds) {
        // 先删除原有关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        articleTagMapper.delete(wrapper);

        // 添加新关联
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
    }

    public List<Tag> getTagsByArticleId(Long articleId) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);

        if (articleTags.isEmpty()) {
            return List.of();
        }

        List<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .toList();

        return tagMapper.selectBatchIds(tagIds);
    }
}
