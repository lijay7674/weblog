DROP TABLE IF EXISTS `blog`;

CREATE TABLE `blog` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `user_id` bigint NOT NULL COMMENT '作者 ID',
                        `title` varchar(200) NOT NULL COMMENT '标题',
                        `content` text NOT NULL COMMENT '内容',
                        `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
                        `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
                        `view_count` int DEFAULT '0' COMMENT '浏览量',
                        `like_count` int DEFAULT '0' COMMENT '点赞数',
                        `comment_count` int DEFAULT '0' COMMENT '评论数',
                        `status` tinyint DEFAULT '1' COMMENT '状态 1 发布 0 草稿',
                        `deleted` tinyint DEFAULT '0',
                        `version` int DEFAULT '1' COMMENT '乐观锁版本号',  -- ← 新增这行
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='博客表';


-- 1. 时间倒序查询索引（博客列表默认按时间倒序）
-- 为什么：列表查询默认按 create_time DESC 排序，复合索引避免 filesort
CREATE INDEX `idx_create_time` ON `blog` (`create_time` DESC);

-- 2. 状态+时间复合索引（只查询已发布博客）
-- 为什么：列表查询需要过滤 status=1，与 create_time 组成复合索引提高筛选效率
CREATE INDEX `idx_status_create_time` ON `blog` (`status`, `create_time` DESC);

-- 3. 用户+状态+时间复合索引（查询某用户的博客列表）
-- 为什么：个人主页查询场景，先过滤 user_id 和 status，再按时间排序
CREATE INDEX `idx_user_status_time` ON `blog` (`user_id`, `status`, `create_time` DESC);
