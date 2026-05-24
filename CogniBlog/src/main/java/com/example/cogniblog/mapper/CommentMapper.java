package com.example.cogniblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cogniblog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM comment WHERE article_id = #{articleId} ORDER BY create_time ASC")
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT * FROM comment WHERE parent_id IS NULL AND article_id = #{articleId} ORDER BY create_time DESC")
    IPage<Comment> selectParentComments(Page<Comment> page, @Param("articleId") Long articleId);
}
