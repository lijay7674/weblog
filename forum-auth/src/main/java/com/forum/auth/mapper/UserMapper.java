package com.forum.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，自动拥有 CRUD 方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // BaseMapper 已提供以下方法：
    // - insert(entity) 插入
    // - deleteById(id) 根据ID删除
    // - updateById(entity) 根据ID更新
    // - selectById(id) 根据ID查询
    // - selectOne(wrapper) 条件查询单条
    // - selectList(wrapper) 条件查询多条
    // - selectPage(page, wrapper) 分页查询
}
