package com.github.yuxiaobin.mybatis.gm.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

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

import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.test.entity.persistent.UserWithDateVersion;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-test-optlock.xml" })
public class OptLockerDateVersionTest {
	
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
		String filePath = OptLockerDateVersionTest.class.getClassLoader().getResource("").getPath()+"/"+filename;
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
		String filePath = OptLockerDateVersionTest.class.getClassLoader().getResource("").getPath()+"/"+filename;
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
	public void updateVersionCorrect(){
		UserWithDateVersion user = new UserWithDateVersion();
		user.setId(1L);
		user.setAge(18);
		user.setName("Tomcat");
		user.setPrice(new BigDecimal("9.99"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH,-1);
		user.setTestDate(cal.getTime());
		user.setTestType(1);
		user.setVersion(1);
		int effRow = generalMapper.insert(user);
		Assert.assertEquals(1, effRow);

		UserWithDateVersion userDB = generalMapper.selectById(user.getId(), UserWithDateVersion.class);
		userDB.setVersion(3);
		Assert.assertEquals(1,generalMapper.updateById(userDB));

		userDB = generalMapper.selectById(user.getId(), UserWithDateVersion.class);
		Assert.assertEquals(3,userDB.getVersion().intValue());
		Date testDate = userDB.getTestDate();
		Assert.assertTrue(System.currentTimeMillis()-testDate.getTime()<100);

	}

}
