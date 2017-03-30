package com.github.yuxiaobin.mybatis.gm.test.entity.mapper;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {
	
	@Select(
			"select count(1) from user"
			)
	@ResultType(Integer.class)
	public Integer getCountByMapper();

}
