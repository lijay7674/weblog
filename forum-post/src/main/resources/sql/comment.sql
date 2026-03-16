CREATE TABLE `comment` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `blog_id` bigint NOT NULL COMMENT '博客ID',
                           `user_id` bigint NOT NULL COMMENT '评论者ID',
                           `parent_id` bigint DEFAULT '0' COMMENT '父评论ID',
                           `content` varchar(1000) NOT NULL COMMENT '评论内容',
                           `like_count` int DEFAULT '0' COMMENT '点赞数',
                           `deleted` tinyint DEFAULT '0',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           KEY `idx_blog_id` (`blog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

-- 1. 博客ID+父ID复合索引（查询某博客的评论列表）
-- 为什么：评论列表查询需要过滤 blog_id 和 parent_id（区分一级/二级评论）
CREATE INDEX `idx_blog_parent` ON `comment` (`blog_id`, `parent_id`, `create_time` DESC);

-- 2. 父评论ID索引（查询某条评论的回复列表）
-- 为什么：查看回复时需要根据 parent_id 查询
CREATE INDEX `idx_parent_id` ON `comment` (`parent_id`);

-- 3. 用户ID索引（查询某用户的所有评论）
-- 为什么：个人中心查看"我的评论"场景
CREATE INDEX `idx_user_id` ON `comment` (`user_id`, `create_time` DESC);