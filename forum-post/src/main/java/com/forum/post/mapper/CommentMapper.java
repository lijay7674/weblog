package com.forum.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.post.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论 Mapper 接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 增加点赞数
     * @param commentId 评论ID
     * @param increment 增量（可为负数）
     * @return 影响行数
     */
    int incrementLikeCount(@Param("commentId") Long commentId, @Param("increment") Integer increment);

    /**
     * 统计某博客的评论数
     * @param blogId 博客ID
     * @return 评论数
     */
    @Select("SELECT COUNT(*) FROM comment WHERE blog_id = #{blogId} AND deleted = 0")
    Long countByBlogId(@Param("blogId") Long blogId);

    /**
     * 统计某评论的回复数
     * @param parentId 父评论ID
     * @return 回复数
     */
    @Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{parentId} AND deleted = 0")
    Long countByParentId(@Param("parentId") Long parentId);
}
