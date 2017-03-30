package com.github.yuxiaobin.mybatis.gm.test;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.test.entity.mapper.UserMapper;
import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.User;
import com.github.yuxiaobin.mybatis.gm.test.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-test.xml" })
public class UserTest {
	
	@Autowired
	UserService userService;
	
	@Autowired
	GeneralMapper generalMapper;
	@Autowired
	UserMapper userMapper;
	
	@Before
	public void clearData(){
		userService.deleteAll();
	}
	
	@Test
	public void insertWithIDTest(){
		User user = new User();
		user.setId(1L);
		user.setAge(18);
		user.setName("Tomcat");
		user.setPrice(new BigDecimal("9.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		boolean result = userService.addUser(user);
		Assert.assertTrue(result);
		Assert.assertEquals(1, user.getId().intValue());
	}
	
	@Test
	public void insertWithoutIDTest(){
		User user = new User();
		user.setId(null);
		user.setAge(18);
		user.setName("Jerry");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		boolean result = userService.addUser(user);
		Assert.assertTrue(result);
		Assert.assertNotNull(user.getId());
	}
	
	@Test
	public void updateTest(){
		User user = new User();
		user.setId(null);
		user.setAge(18);
		user.setName("Jerry");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		boolean result = userService.addUser(user);
		Assert.assertTrue(result);
		Long userId = user.getId();
		Assert.assertNotNull(userId);
		
		User updateUser = new User();
		updateUser.setId(userId);
		updateUser.setAge(20);
		generalMapper.updateSelectiveById(updateUser);
		
		updateUser = generalMapper.selectById(userId, User.class);
		
		Assert.assertEquals(20, updateUser.getAge().intValue());
		Assert.assertEquals(1, user.getTestType().intValue());//not updated
		
		
	}
	
	@Test
	public void mapperMethodTest(){
		User user = new User();
		user.setId(null);
		user.setAge(18);
		user.setName("Jerry2");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		userService.addUser(user);
		
		int count = userMapper.getCountByMapper();
		Assert.assertEquals(1, count);
		
		int count2 = generalMapper.selectCount(new User());
		Assert.assertEquals(1, count2);
		
	}

}
