package com.github.yuxiaobin.mybatis.gm.test.service;

import com.github.yuxiaobin.mybatis.gm.GeneralEntityWrapper;
import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.User;

public interface UserService {
	
	public boolean addUser(User user);
	
	public boolean deleteUserById(Long userId);
	
	public boolean deleteSelective(User user);
	
	public boolean deleteByEW(GeneralEntityWrapper<User> ew);
	
	public boolean deleteAll();
	
	public boolean updateById(User user);
	
	public boolean updateSelectiveById(User user);

}
