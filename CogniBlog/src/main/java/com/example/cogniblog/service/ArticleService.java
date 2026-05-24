package com.example.cogniblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cogniblog.common.BusinessException;
import com.example.cogniblog.dto.ArticleRequest;
import com.example.cogniblog.entity.*;
import com.example.cogniblog.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_LIST_KEY = "article:list:";
    private static final String ARTICLE_DETAIL_KEY = "article:detail:";
    private static final String ARTICLE_VIEW_COUNT_KEY = "article:viewCount:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    public Page<Article> getArticlePage(Integer current, Integer size, Long categoryId, Long tagId, Long userId, String keyword) {
        // 生成缓存 key
        String cacheKey = ARTICLE_LIST_KEY + current + "-" + size + "-" + categoryId + "-" + tagId + "-" + userId + "-" + keyword;

        // 尝试从 Redis 获取缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("文章列表缓存命中: {}", cacheKey);
            return (Page<Article>) cached;
        }

        log.debug("文章列表缓存未命中，查询数据库: {}", cacheKey);
        Page<Article> page = new Page<>(current, size);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (userId != null) {
            wrapper.eq(Article::getUserId, userId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getContent, keyword));
        }
        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> result = articleMapper.selectPage(page, wrapper);

        // 填充关联数据
        for (Article article : result.getRecords()) {
            fillArticleRelations(article);
        }

        // 如果有标签筛选，处理标签关联
        if (tagId != null) {
            List<Article> filtered = result.getRecords().stream()
                    .filter(a -> {
                        List<Tag> tags = getTagsByArticleId(a.getId());
                        return tags.stream().anyMatch(t -> t.getId().equals(tagId));
                    })
                    .collect(Collectors.toList());
            result.setRecords(filtered);
            result.setTotal(filtered.size());
        }

        // 存入 Redis 缓存
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return result;
    }

    public Page<Article> getArticlesByUserId(Long userId, Integer current, Integer size) {
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUserId, userId);
        wrapper.orderByDesc(Article::getCreateTime);
        Page<Article> result = articleMapper.selectPage(page, wrapper);

        for (Article article : result.getRecords()) {
            fillArticleRelations(article);
        }
        return result;
    }

    public Article getArticleById(Long id) {
        // 生成缓存 key
        String cacheKey = ARTICLE_DETAIL_KEY + id;

        // 尝试从 Redis 获取缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("文章详情缓存命中: {}", cacheKey);
            return (Article) cached;
        }

        log.debug("文章详情缓存未命中，查询数据库: {}", cacheKey);
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        fillArticleRelations(article);

        // 存入 Redis 缓存
        redisTemplate.opsForValue().set(cacheKey, article, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return article;
    }

    public Article getArticleDetail(Long id) {
        Article article = getArticleById(id);

        // 浏览量 +1（Redis 计数）
        String key = ARTICLE_VIEW_COUNT_KEY + id;
        Long viewCount = redisTemplate.opsForValue().increment(key);
        if (viewCount != null && viewCount == 1) {
            // 首次访问，从数据库获取并设置
            article.setViewCount(article.getViewCount() + 1);
            articleMapper.updateViewCount(id, article.getViewCount());
        } else {
            article.setViewCount(viewCount != null ? viewCount.intValue() : article.getViewCount());
        }

        return article;
    }

    @Transactional
    public void addArticle(ArticleRequest request, Long userId) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategoryId(request.getCategoryId());
        article.setUserId(userId);
        article.setViewCount(0);
        articleMapper.insert(article);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), request.getTagIds());
        }

        // 清除文章列表缓存
        clearArticleListCache();
    }

    @Transactional
    public void updateArticle(ArticleRequest request) {
        Article article = articleMapper.selectById(request.getId());
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategoryId(request.getCategoryId());
        articleMapper.updateById(article);

        // 更新标签关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, request.getId());
        articleTagMapper.delete(wrapper);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), request.getTagIds());
        }

        // 清除文章列表缓存和文章详情缓存
        clearArticleListCache();
        redisTemplate.delete(ARTICLE_DETAIL_KEY + request.getId());
    }

    @Transactional
    public void deleteArticle(Long id) {
        // 删除文章标签关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(wrapper);

        // 删除文章
        articleMapper.deleteById(id);

        // 清除缓存
        clearArticleListCache();
        redisTemplate.delete(ARTICLE_DETAIL_KEY + id);
        redisTemplate.delete(ARTICLE_VIEW_COUNT_KEY + id);
    }

    /**
     * 清除文章列表缓存（使用通配符删除）
     */
    private void clearArticleListCache() {
        try {
            var keys = redisTemplate.keys(ARTICLE_LIST_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("清除文章列表缓存: {} 条", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除文章列表缓存失败", e);
        }
    }

    private void fillArticleRelations(Article article) {
        // 设置用户信息
        User user = userMapper.selectById(article.getUserId());
        if (user != null) {
            user.setPassword(null);
            article.setUser(user);
        }

        // 设置分类信息
        Category category = categoryMapper.selectById(article.getCategoryId());
        article.setCategory(category);

        // 设置标签列表
        List<Tag> tags = getTagsByArticleId(article.getId());
        article.setTags(tags);
    }

    private List<Tag> getTagsByArticleId(Long articleId) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);

        if (articleTags.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());

        return tagMapper.selectBatchIds(tagIds);
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }
}
