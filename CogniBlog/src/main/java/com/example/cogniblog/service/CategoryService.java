package com.example.cogniblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cogniblog.common.BusinessException;
import com.example.cogniblog.entity.Article;
import com.example.cogniblog.entity.Category;
import com.example.cogniblog.mapper.ArticleMapper;
import com.example.cogniblog.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public List<Category> getAllCategories() {
        return categoryMapper.selectList(null);
    }

    @Cacheable(value = "category", key = "#id")
    public Category getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    @CacheEvict(value = "category", allEntries = true)
    public void addCategory(Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, category.getName());
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }
        categoryMapper.insert(category);
    }

    @CacheEvict(value = "category", allEntries = true)
    public void updateCategory(Category category) {
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }
        categoryMapper.updateById(category);
    }

    @CacheEvict(value = "category", allEntries = true)
    public void deleteCategory(Long id) {
        // 检查是否有文章引用该分类
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCategoryId, id);
        if (articleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该分类下有文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    public List<Article> getArticlesByCategory(Long categoryId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCategoryId, categoryId);
        wrapper.orderByDesc(Article::getCreateTime);
        return articleMapper.selectList(wrapper);
    }
}
