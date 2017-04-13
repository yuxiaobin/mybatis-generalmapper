package com.github.yuxiaobin.mybatis.gm.conf;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.baomidou.mybatisplus.MybatisConfiguration;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.github.yuxiaobin.mybatis.gm.conf.DBKeywordsConfig.DBKeywords;
import com.github.yuxiaobin.mybatis.gm.mapper.GeneralMapperSqlInjector;
import com.github.yuxiaobin.mybatis.gm.processer.MybatisGeneralEntityProcessor;
import com.github.yuxiaobin.mybatis.gm.utils.GeneralJdbcReflectionUtil;
import com.github.yuxiaobin.mybatis.gm.utils.GeneralJdbcReflectionUtil.DBType;

/**
 * @author Kelly Lake(179634696@qq.com)
 */
@Configuration
@Import(DBKeywordsConfig.class)
public class GeneralMapperBootstrapConfiguration {

    @Bean
    public GeneralMapperSqlInjector generalSqlInjector(DataSource ds, DBKeywords keywords) {
    	GeneralMapperSqlInjector sqlInjector = new GeneralMapperSqlInjector();
    	try(Connection conn = ds.getConnection();){
    		DBType dbType = GeneralJdbcReflectionUtil.getDbType(conn.getMetaData().getURL());
    		switch (dbType) {
    		case MYSQL:
    			sqlInjector.setDBType(com.baomidou.mybatisplus.mapper.DBType.MYSQL);
    			MybatisConfiguration.DB_TYPE = com.baomidou.mybatisplus.mapper.DBType.MYSQL;
    			break;
    		case ORACLE:
    			sqlInjector.setDBType(com.baomidou.mybatisplus.mapper.DBType.ORACLE);
    			MybatisConfiguration.DB_TYPE = com.baomidou.mybatisplus.mapper.DBType.ORACLE;
    			break;
    		default:
    			sqlInjector.setDBType(com.baomidou.mybatisplus.mapper.DBType.MYSQL);
    			MybatisConfiguration.DB_TYPE = com.baomidou.mybatisplus.mapper.DBType.MYSQL;
    			break;
    		}
		} catch (SQLException e) {
			throw new MybatisPlusException(e);
		}
    	if(keywords!=null){
    		sqlInjector.addKeyWords(keywords.getKeywords());
    	}
		return sqlInjector;
    }

    @Bean
    public MybatisGeneralEntityProcessor mybatisEntityProcessor(GeneralMapperSqlInjector generalMapperSqlInjector) {
    	return new MybatisGeneralEntityProcessor(generalMapperSqlInjector);
    }
}
