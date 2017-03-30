package com.github.yuxiaobin.mybatis.gm.test.service;

import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.User;

public interface UserService {
	
	public boolean addUser(User user);
	
	public boolean deleteUserById(Long userId);
	
	public boolean deleteAll();

}
