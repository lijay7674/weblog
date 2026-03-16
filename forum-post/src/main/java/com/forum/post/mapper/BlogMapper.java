package com.forum.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.post.entity.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 博客 Mapper 接口
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

    /**
     * 增加浏览量
     * @param blogId 博客ID
     * @param increment 增量
     * @return 影响行数
     */
    int incrementViewCount(@Param("blogId") Long blogId, @Param("increment") Integer increment);

    /**
     * 增加点赞数
     * @param blogId 博客ID
     * @param increment 增量（可为负数）
     * @return 影响行数
     */
    int incrementLikeCount(@Param("blogId") Long blogId, @Param("increment") Integer increment);

    /**
     * 增加评论数
     * @param blogId 博客ID
     * @param increment 增量（可为负数）
     * @return 影响行数
     */
    int incrementCommentCount(@Param("blogId") Long blogId, @Param("increment") Integer increment);
}
