-- ============================================
-- CogniBlog 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cogniblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cogniblog;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色: admin=管理员, user=普通用户',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 分类表 (category)
-- ============================================
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================
-- 3. 标签表 (tag)
-- ============================================
DROP TABLE IF EXISTS tag;
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================
-- 4. 文章表 (article)
-- ============================================
DROP TABLE IF EXISTS article;
CREATE TABLE article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    content TEXT NOT NULL COMMENT '文章内容',
    user_id BIGINT NOT NULL COMMENT '作者ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ============================================
-- 5. 文章-标签关联表 (article_tag)
-- ============================================
DROP TABLE IF EXISTS article_tag;
CREATE TABLE article_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    INDEX idx_article_id (article_id),
    INDEX idx_tag_id (tag_id),
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章-标签关联表';

-- ============================================
-- 6. 评论表 (comment)
-- ============================================
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID(楼中楼)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 初始数据
-- ============================================
-- 插入默认分类
INSERT INTO category (name, description) VALUES
('技术', '编程、开发、架构等技术相关文章'),
('生活', '日常生活、随笔、感悟'),
('其他', '其他类型的文章');

-- 插入默认标签
INSERT INTO tag (name) VALUES
('Java'), ('SpringBoot'), ('Vue'), ('MySQL'), ('Redis'), ('AI');

-- 插入测试用户 (密码是 123456 的 MD5, role: admin=管理员, user=普通用户)
INSERT INTO user (username, password, nickname, avatar, role) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', NULL, 'admin'),
('test', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', NULL, 'user');

-- 插入测试文章
INSERT INTO article (title, content, user_id, category_id, view_count) VALUES
('欢迎使用 CogniBlog', '这是一篇欢迎文章，欢迎大家使用 CogniBlog 智能博客系统！\n\n系统特性：\n1. 完整的博客功能\n2. AI 智能助手\n3. Redis 缓存优化', 1, 1, 100),
('SpringBoot 入门教程', '本文介绍 SpringBoot 的基本使用...', 1, 1, 50),
('Vue3 组件化开发', 'Vue3 带来了 Composition API...', 2, 1, 30);

-- 绑定文章标签
INSERT INTO article_tag (article_id, tag_id) VALUES
(1, 1), (1, 6),
(2, 1), (2, 2),
(3, 3);

-- 插入测试评论
INSERT INTO comment (article_id, user_id, content, parent_id) VALUES
(1, 2, '写得很好！', NULL),
(1, 1, '谢谢支持！', 1),
(2, 2, '期待更新', NULL);

-- ============================================
-- 验证表结构
-- ============================================
SHOW TABLES;
