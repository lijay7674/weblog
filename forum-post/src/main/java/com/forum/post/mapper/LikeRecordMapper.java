package com.forum.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.post.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点赞记录 Mapper 接口
 */
@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    /**
     * 统计某目标的点赞数
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 点赞数
     */
    @Select("SELECT COUNT(*) FROM like_record WHERE target_id = #{targetId} AND target_type = #{targetType}")
    Long countByTarget(@Param("targetId") Long targetId, @Param("targetType") Integer targetType);

    /**
     * 批量插入点赞记录（忽略重复）
     * @param records 点赞记录列表
     * @return 影响行数
     */
    int batchInsertOrIgnore(@Param("records") List<LikeRecord> records);

    /**
     * 根据复合键批量删除
     * @param userIds 用户ID列表
     * @param targetIds 目标ID列表
     * @param targetType 目标类型
     * @return 影响行数
     */
    int batchDeleteByCompositeKey(@Param("userIds") List<Long> userIds, 
                                   @Param("targetIds") List<Long> targetIds, 
                                   @Param("targetType") Integer targetType);
}
