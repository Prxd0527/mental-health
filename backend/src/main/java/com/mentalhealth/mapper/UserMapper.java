package com.mentalhealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mentalhealth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
