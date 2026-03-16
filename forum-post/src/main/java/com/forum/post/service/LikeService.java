package com.forum.post.service;

import java.util.List;
import java.util.Map;

/**
 * 点赞服务接口
 */
public interface LikeService {

    /**
     * 点赞/取消点赞
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型 1博客 2评论
     * @param liked true点赞 false取消
     * @return 当前点赞状态
     */
    Boolean like(Long userId, Long targetId, Integer targetType, Boolean liked);

    /**
     * 检查用户是否点赞
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 是否点赞
     */
    Boolean checkUserLiked(Long userId, Long targetId, Integer targetType);

    /**
     * 批量检查用户点赞状态
     * @param userId 用户ID
     * @param targetIds 目标ID列表
     * @param targetType 目标类型
     * @return key: targetId, value: 是否点赞
     */
    Map<Long, Boolean> batchCheckUserLiked(Long userId, List<Long> targetIds, Integer targetType);

    /**
     * 获取目标的点赞数
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 点赞数
     */
    Long getLikeCount(Long targetId, Integer targetType);

    /**
     * 同步点赞数据到数据库（单条）
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param liked 是否点赞
     */
    void syncToDatabase(Long userId, Long targetId, Integer targetType, Boolean liked);

    /**
     * 批量同步点赞数据到数据库
     */
    void batchSyncToDatabase();
}
