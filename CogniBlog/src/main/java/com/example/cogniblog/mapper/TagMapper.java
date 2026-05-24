package com.example.cogniblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cogniblog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
