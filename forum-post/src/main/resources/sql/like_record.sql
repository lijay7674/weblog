CREATE TABLE `like_record` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `user_id` bigint NOT NULL,
                               `target_id` bigint NOT NULL COMMENT '目标ID(博客/评论)',
                               `target_type` tinyint NOT NULL COMMENT '类型 1博客 2评论',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录表'

-- 1. 目标ID+类型索引（查询某博客/评论的点赞数）
-- 为什么：统计点赞数时需要根据 target_id + target_type 查询
CREATE INDEX `idx_target_type` ON `like_record` (`target_id`, `target_type`);

-- 2. 用户ID+时间索引（查询用户的点赞历史）
-- 为什么：个人中心查看"我的点赞"场景
CREATE INDEX `idx_user_time` ON `like_record` (`user_id`, `create_time` DESC);