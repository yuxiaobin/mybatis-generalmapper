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
import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.UserWithIntegerVersion;
import com.github.yuxiaobin.mybatis.gm.test.entity.vo.UserVO;
import com.github.yuxiaobin.mybatis.gm.test.entity.vo.UserWithIntegerVersionVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-test-optlock.xml" })
public class OptLockerIntegerVersionTest {
	
	@BeforeClass
	public static void initDB() throws SQLException {
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-test-optlock.xml");
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
		String filePath = OptLockerIntegerVersionTest.class.getClassLoader().getResource("").getPath()+"/"+filename;
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
		String filePath = OptLockerIntegerVersionTest.class.getClassLoader().getResource("").getPath()+"/"+filename;
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
	GeneralMapper generalMapper;

	@Test
	public void insertWithIDTest(){
		UserWithIntegerVersion user = new UserWithIntegerVersion();
		user.setId(1L);
		user.setAge(18);
		user.setName("Tomcat");
		user.setPrice(new BigDecimal("9.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		int effRow = generalMapper.insert(user);
		Assert.assertEquals(1, effRow);
	}

	@Test
	public void insertWithoutIDTest(){
		UserWithIntegerVersion user = new UserWithIntegerVersion();
		user.setId(null);
		user.setAge(18);
		user.setName("Jerry");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		int effRow = generalMapper.insert(user);
		Assert.assertEquals(1, effRow);
		Assert.assertNotNull(user.getId());
	}

	@Test
	public void testUpdateByIdVersionFailed(){
		UserWithIntegerVersion user = new UserWithIntegerVersion();
		user.setId(null);
		user.setAge(18);
		user.setName("Jerry");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		int effRow = generalMapper.insert(user);
		Assert.assertEquals(1, effRow);
		Long userId = user.getId();
		Assert.assertNotNull(userId);

		UserWithIntegerVersion updateUser = new UserWithIntegerVersion();
		updateUser.setId(userId);
		updateUser.setAge(20);
		updateUser.setVersion(2);
		Assert.assertEquals(0, generalMapper.updateById(updateUser));

		updateUser.setVersion(1);
		Assert.assertEquals(1, generalMapper.updateById(updateUser));
		UserWithIntegerVersion db = generalMapper.selectById(userId,UserWithIntegerVersion.class);
		Assert.assertEquals(2,db.getVersion().intValue());
	}

	@Test
	public void testUpdateSeletiveByIdVersionFailed(){
		UserWithIntegerVersion user = new UserWithIntegerVersion();
		user.setId(null);
		user.setAge(18);
		user.setName("Jerry");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		int effRow = generalMapper.insert(user);
		Assert.assertEquals(1, effRow);
		Long userId = user.getId();
		Assert.assertNotNull(userId);

		UserWithIntegerVersion updateUser = new UserWithIntegerVersion();
		updateUser.setId(userId);
		updateUser.setAge(20);
		updateUser.setVersion(2);
		Assert.assertEquals(0, generalMapper.updateSelectiveById(updateUser));

		updateUser.setVersion(1);
		Assert.assertEquals(1, generalMapper.updateSelectiveById(updateUser));
		UserWithIntegerVersion db = generalMapper.selectById(userId,UserWithIntegerVersion.class);
		Assert.assertEquals(2,db.getVersion().intValue());

		db.setName("111222");
		Assert.assertEquals(1, generalMapper.updateSelectiveById(db));
		db = generalMapper.selectById(userId,UserWithIntegerVersion.class);
		Assert.assertEquals(3,db.getVersion().intValue());
		Assert.assertEquals("111222",db.getName());
	}

	@Test
	public void updateTest(){
		UserWithIntegerVersion user = new UserWithIntegerVersion();
		user.setId(null);
		user.setAge(18);
		user.setName("Jerry");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		int effRow = generalMapper.insert(user);
		Assert.assertEquals(1, effRow);
		Long userId = user.getId();
		Assert.assertNotNull(userId);

		UserWithIntegerVersion updateUser = new UserWithIntegerVersion();
		updateUser.setId(userId);
		updateUser.setAge(20);
		updateUser.setVersion(1);
		generalMapper.updateSelectiveById(updateUser);
		
		updateUser = generalMapper.selectById(userId, UserWithIntegerVersion.class);
		
		Assert.assertEquals(20, updateUser.getAge().intValue());
		Assert.assertEquals(1, updateUser.getTestType().intValue());//not updated
		Assert.assertEquals(2, updateUser.getVersion().intValue());//optlocker
	}

	@Test
	public void testUpdateSeletive(){
		UserWithIntegerVersion user = new UserWithIntegerVersion();
		user.setId(null);
		user.setAge(18);
		user.setName("testUpdateSeletive");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		Assert.assertEquals(1, generalMapper.insert(user));
		Assert.assertNotNull(user.getId());
		user = new UserWithIntegerVersion();
		user.setId(null);
		user.setAge(18);
		user.setName("testUpdateSeletive");
		user.setPrice(new BigDecimal("19.99"));
		user.setTestDate(new Date());
		user.setTestType(1);
		user.setVersion(1);
		Assert.assertEquals(1, generalMapper.insert(user));
		Assert.assertNotNull(user.getId());

		UserWithIntegerVersion updUser = new UserWithIntegerVersion();
		updUser.setName("after");
		UserWithIntegerVersion whereUser = new UserWithIntegerVersion();
		whereUser.setName("testUpdateSeletive");
		whereUser.setVersion(1);
		Assert.assertEquals(2, generalMapper.updateSelective(updUser, whereUser));
		List<UserWithIntegerVersion> userList = generalMapper.selectList(new GeneralEntityWrapper<>(updUser));
		Assert.assertNotNull(userList);
		Assert.assertEquals(2, userList.size());
		for(UserWithIntegerVersion u:userList){
			Assert.assertEquals(1, u.getVersion().intValue());
		}

	}

	@Test
	public void entityWrapperTest(){
		UserWithIntegerVersion userParm = new UserWithIntegerVersion();
		GeneralEntityWrapper<UserWithIntegerVersion> ew = new GeneralEntityWrapper<>(userParm);
		ew.and("test_date>{0} and test_date<{1}", LocalDate.of(2016, 1, 1), LocalDate.of(2017, 6, 3));
		List<UserWithIntegerVersion> list = generalMapper.selectPage(new Page<UserWithIntegerVersion>(1,3), ew);
		Assert.assertEquals(3, list.size());

		UserWithIntegerVersion userVOParm = new UserWithIntegerVersionVO();
		ew = new GeneralEntityWrapper<>(userVOParm);
		ew.and("test_date>#{startDate} and test_date<#{endDate}", LocalDate.of(2016, 1, 1), LocalDate.of(2017, 6, 3));
		Assert.assertEquals(3, generalMapper.selectPage(new Page<UserVO>(1,3), ew).size());
	}
	
	@Test
	public void testEwSqlSelect(){
		UserWithIntegerVersion parm = new UserWithIntegerVersion();
		GeneralEntityWrapper<UserWithIntegerVersion> ew = new GeneralEntityWrapper<>(parm,"test_id as id, test_date");
		List<UserWithIntegerVersion> list = generalMapper.selectPage(new Page<UserWithIntegerVersion>(1,3), ew);
		list.forEach((x)->{
			Assert.assertNotNull(x.getId());
			Assert.assertNotNull(x.getTestDate());
		});
	}
	
	@Test
	public void userVOTest(){
		UserWithIntegerVersionVO userVO = new UserWithIntegerVersionVO();
		userVO.setId(12311L);
		userVO.setName("userVO");
		userVO.setAge(19);
		userVO.setPrice(new BigDecimal("19.99"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		userVO.setTestDate(cal.getTime());
		userVO.setTestType(1);
		userVO.setVersion(1);
		generalMapper.insert(userVO);

		UserWithIntegerVersion user = generalMapper.selectById(12311L, UserWithIntegerVersion.class);
		Assert.assertNotNull(user);
	}
	
	@Test
	public void testUpdate(){
		UserWithIntegerVersionVO userVO = new UserWithIntegerVersionVO();
		userVO.setName("userVO");
		userVO.setAge(19);
		userVO.setPrice(new BigDecimal("19.99"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		userVO.setTestDate(cal.getTime());
		userVO.setTestType(1);
		userVO.setVersion(1);
		generalMapper.insert(userVO);

		UserWithIntegerVersion user = new UserWithIntegerVersion();
		user.setId(userVO.getId());
		user.setPrice(new BigDecimal("9.99"));
		user.setVersion(userVO.getVersion());
		
		generalMapper.updateSelectiveById(user);
		user = generalMapper.selectById(userVO.getId(), UserWithIntegerVersion.class);
		Assert.assertEquals(new BigDecimal("9.99"), user.getPrice());
		Assert.assertEquals(2, user.getVersion().intValue());
		Assert.assertNotNull(user.getTestDate());
		
		user.setAge(28);
		user.setTestDate(null);
		generalMapper.updateById(user);
		user = generalMapper.selectById(userVO.getId(), UserWithIntegerVersion.class);
		Assert.assertEquals(28, user.getAge().intValue());
		Assert.assertEquals(3, user.getVersion().intValue());
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
		generalMapper.insert(userVO);

		UserWithIntegerVersion user = generalMapper.selectById(userVO.getId(), UserWithIntegerVersion.class);
		Assert.assertNotNull(user);
		generalMapper.deleteById(userVO.getId(), UserWithIntegerVersion.class);
		user = generalMapper.selectById(userVO.getId(), UserWithIntegerVersion.class);
		Assert.assertNull(user);
		
		/*
		 * deleteSelective
		 */
		UserWithIntegerVersion user2 = new UserWithIntegerVersion();
		user2.setName("userVO");
		user2.setAge(19);
		user2.setPrice(new BigDecimal("19.99"));
		user2.setTestDate(new Date());
		user2.setTestType(1);
		user2.setVersion(1);
		generalMapper.insert(userVO);

		UserWithIntegerVersion userParm = new UserWithIntegerVersion();
		userParm.setAge(19);
		generalMapper.deleteSelective(userParm);
		Assert.assertNull(generalMapper.selectById(user2.getId(), UserWithIntegerVersion.class));
		
		/*
		 * deleteByEW
		 */
		UserWithIntegerVersion user3 = new UserWithIntegerVersion();
		user3.setName("userVO");
		user3.setAge(19);
		user3.setPrice(new BigDecimal("19.99"));
		user3.setTestDate(new Date());
		user3.setTestType(1);
		user3.setVersion(1);
		generalMapper.insert(userVO);

		UserWithIntegerVersion userParm2 = new UserWithIntegerVersion();
		GeneralEntityWrapper<UserWithIntegerVersion> ew = new GeneralEntityWrapper<>(userParm2);
		ew.where("age={0}", 19);
		generalMapper.deleteByEW(ew);
		Assert.assertNull(generalMapper.selectById(user3.getId(), UserWithIntegerVersion.class));
		
	}
	
}
