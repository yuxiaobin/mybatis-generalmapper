package com.github.yuxiaobin.mybatis.gm.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.yuxiaobin.mybatis.gm.GeneralEntityWrapper;
import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.test.entity.mapper.UserMapper;
import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.User;
import com.github.yuxiaobin.mybatis.gm.test.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-test.xml" })
public class UserTest {
	
	@BeforeClass
	public static void initDB() throws SQLException {
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-test.xml");
		DataSource ds = (DataSource) context.getBean("dataSource");
		try (Connection conn = ds.getConnection();) {
			String createTableSql = readFile("user.ddl.sql");
			Statement stmt = conn.createStatement();
			stmt.execute(createTableSql);
			conn.commit();
			stmt.execute("truncate table user");
			insertUsers(stmt);
			conn.commit();
		} 
	}
	
	private static void insertUsers(Statement stmt) throws SQLException{
		String filename = "user.insert.sql";
		String filePath = UserTest.class.getClassLoader().getResource("").getPath()+"/"+filename;
		try (
				BufferedReader reader = new BufferedReader(new FileReader(filePath));
				) {
			String line = null;
			while ((line = reader.readLine()) != null){
				stmt.execute(line.replace(";", ""));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String readFile(String filename) {
		StringBuilder builder = new StringBuilder();
		String filePath = UserTest.class.getClassLoader().getResource("").getPath()+"/"+filename;
		try (
				BufferedReader reader = new BufferedReader(new FileReader(filePath));
				) {
			String line = null;
			while ((line = reader.readLine()) != null)
				builder.append(line).append(" ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	@Autowired
	UserService userService;

	@Autowired
	GeneralMapper generalMapper;
	@Autowired
	UserMapper userMapper;

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
		int count2 = generalMapper.selectCount(new User());
		Assert.assertEquals(count, count2);
		
	}

	@Test
	public void wrapperTest(){
		User userParm = new User();
		GeneralEntityWrapper<User> ew = new GeneralEntityWrapper<User>(userParm);
		ew.and("test_date>{0} and test_date<{1}", LocalDate.of(2016, 1, 1), LocalDate.of(2017, 6, 3));
		List<User> list = generalMapper.selectPage(new Page<User>(1,3), ew);
		Assert.assertEquals(3, list.size());
	}
	
}
