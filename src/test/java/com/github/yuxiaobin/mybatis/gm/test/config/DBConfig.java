package com.github.yuxiaobin.mybatis.gm.test.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mysql.jdbc.Driver;

/**
 * 对应的数据库配置
 * 
 * @author yuxiaobin
 *
 */
@Configuration
@EnableTransactionManagement
public class DBConfig {

	@Bean
    public DataSource dataSource() throws SQLException {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setDriver(new Driver());
        dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis-plus?useUnicode=true&characterEncoding=UTF8");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        return dataSource;
    }
	
	@Bean
	public DataSourceTransactionManager transactionManager(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

}
