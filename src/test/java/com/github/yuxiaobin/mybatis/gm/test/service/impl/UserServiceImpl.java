package com.github.yuxiaobin.mybatis.gm.test.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.yuxiaobin.mybatis.gm.GeneralEntityWrapper;
import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.User;
import com.github.yuxiaobin.mybatis.gm.test.service.UserService;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	GeneralMapper generalMapper;
	
	@Override
	@Transactional
	public boolean addUser(User user) {
		int effectRow = generalMapper.insert(user);
		if(effectRow==0){
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public boolean deleteUserById(Long userId) {
		int effRow = generalMapper.deleteById(userId, User.class);
		return effRow!=0;
	}

	@Override
	@Transactional
	public boolean deleteAll() {
		GeneralEntityWrapper<User> ew = new GeneralEntityWrapper<>(new User());
		ew.and("1=1");
		generalMapper.deleteByEW(ew);
		return true;
	}

	/**
	 * update all fields.
	 * @param user
	 * @return
	 */
	@Override
	@Transactional
	public boolean updateById(User user) {
		generalMapper.updateById(user);
		return true;
	}

	/**
	 * Only update not-null fields.
	 * @param user
	 * @return
	 */
	@Override
	@Transactional
	public boolean updateSelectiveById(User user) {
		generalMapper.updateSelectiveById(user);
		return false;
	}

	@Override
	@Transactional
	public boolean deleteSelective(User user) {
		generalMapper.deleteSelective(user);
		return true;
	}

	@Override
	@Transactional
	public boolean deleteByEW(GeneralEntityWrapper<User> ew) {
		generalMapper.deleteByEW(ew);
		return true;
	}

}
