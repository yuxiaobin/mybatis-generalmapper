package com.github.yuxiaobin.mybatis.gm.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Calendar;
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
import com.github.yuxiaobin.mybatis.gm.test.entity.vo.UserVO;
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
	public void entityWrapperTest(){
		User userParm = new User();
		GeneralEntityWrapper<User> ew = new GeneralEntityWrapper<User>(userParm);
		ew.and("test_date>{0} and test_date<{1}", LocalDate.of(2016, 1, 1), LocalDate.of(2017, 6, 3));
		List<User> list = generalMapper.selectPage(new Page<User>(1,3), ew);
		Assert.assertEquals(3, list.size());
		
		UserVO userVOParm = new UserVO();
		ew = new GeneralEntityWrapper<>(userVOParm);
		ew.and("test_date>#{startDate} and test_date<#{endDate}", LocalDate.of(2016, 1, 1), LocalDate.of(2017, 6, 3));
		Assert.assertEquals(3, generalMapper.selectPage(new Page<UserVO>(1,3), ew).size());
	}
	
	@Test
	public void userVOTest(){
		UserVO userVO = new UserVO();
		userVO.setId(123L);
		userVO.setName("userVO");
		userVO.setAge(19);
		userVO.setPrice(new BigDecimal("19.99"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		userVO.setTestDate(cal.getTime());
		userVO.setTestType(1);
		userVO.setVersion(1);
		userService.addUser(userVO);
		
		User user = generalMapper.selectById(123L, User.class);
		Assert.assertNotNull(user);
	}
	
	@Test
	public void testUpdate(){
		UserVO userVO = new UserVO();
		userVO.setName("userVO");
		userVO.setAge(19);
		userVO.setPrice(new BigDecimal("19.99"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		userVO.setTestDate(cal.getTime());
		userVO.setTestType(1);
		userVO.setVersion(1);
		userService.addUser(userVO);
		
		User user = new User();
		user.setId(userVO.getId());
		user.setPrice(new BigDecimal("9.99"));
		
		userService.updateSelectiveById(user);
		user = generalMapper.selectById(userVO.getId(), User.class);
		Assert.assertEquals(new BigDecimal("9.99"), user.getPrice());
		Assert.assertNotNull(user.getTestDate());
		
		user.setAge(28);
		user.setTestDate(null);
		userService.updateById(user);
		user = generalMapper.selectById(userVO.getId(), User.class);
		Assert.assertEquals(28, user.getAge().intValue());
		Assert.assertNull(user.getTestDate());
		
	}
	
	@Test
	public void testDelete(){
		/*
		 * deleteById
		 */
		UserVO userVO = new UserVO();
		userVO.setName("userVO");
		userVO.setAge(19);
		userVO.setPrice(new BigDecimal("19.99"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		userVO.setTestDate(cal.getTime());
		userVO.setTestType(1);
		userVO.setVersion(1);
		userService.addUser(userVO);
		
		User user = generalMapper.selectById(userVO.getId(), User.class);
		Assert.assertNotNull(user);
		userService.deleteUserById(userVO.getId());
		user = generalMapper.selectById(userVO.getId(), User.class);
		Assert.assertNull(user);
		
		/*
		 * deleteSelective
		 */
		User user2 = new User();
		user2.setName("userVO");
		user2.setAge(19);
		user2.setPrice(new BigDecimal("19.99"));
		user2.setTestDate(new Date());
		user2.setTestType(1);
		user2.setVersion(1);
		userService.addUser(userVO);
		
		User userParm = new User();
		userParm.setAge(19);
		userService.deleteSelective(userParm);
		Assert.assertNull(generalMapper.selectById(user2.getId(), User.class));
		
		/*
		 * deleteByEW
		 */
		User user3 = new User();
		user3.setName("userVO");
		user3.setAge(19);
		user3.setPrice(new BigDecimal("19.99"));
		user3.setTestDate(new Date());
		user3.setTestType(1);
		user3.setVersion(1);
		userService.addUser(userVO);
		
		User userParm2 = new User();
		GeneralEntityWrapper<User> ew = new GeneralEntityWrapper<>(userParm2);
		ew.where("age={0}", 19);
		userService.deleteByEW(ew);
		Assert.assertNull(generalMapper.selectById(user3.getId(), User.class));
		
	}
	
}
