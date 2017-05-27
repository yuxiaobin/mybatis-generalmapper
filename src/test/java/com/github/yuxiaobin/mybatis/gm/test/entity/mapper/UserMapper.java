package com.github.yuxiaobin.mybatis.gm.test.entity.mapper;

import java.util.List;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.User;

public interface UserMapper {
	
	@Select(
			"select count(1) from user"
			)
	@ResultType(Integer.class)
	public Integer getCountByMapper();
	
	@Select(
			"select * from user"
			)
	public List<User> selectUsers();

}
