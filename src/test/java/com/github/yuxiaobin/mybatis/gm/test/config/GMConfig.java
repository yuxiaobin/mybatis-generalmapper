package com.github.yuxiaobin.mybatis.gm.test.config;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.baomidou.mybatisplus.MybatisConfiguration;
import com.github.yuxiaobin.mybatis.gm.GeneralMapper;
import com.github.yuxiaobin.mybatis.gm.GeneralSqlSessionFactoryBean;
import com.github.yuxiaobin.mybatis.gm.conf.GeneralMapperBootstrapConfiguration;
import com.github.yuxiaobin.mybatis.gm.intcpt.GeneralPaginationInterceptor;
import com.github.yuxiaobin.mybatis.gm.plus.GeneralMybatisXMLLanguageDriver;

/**
 * 
 * @author yuxiaobin
 *
 */
@Configuration
@ComponentScan({"com.github.yuxiaobin.mybatis.gm.test.service"})
@MapperScan("com.github.yuxiaobin.mybatis.gm.test.entity.mapper")
@Import({GeneralMapperBootstrapConfiguration.class})
public class GMConfig {

	@Bean
	public GeneralSqlSessionFactoryBean sqlSessionFactory (DataSource dataSource){
		GeneralSqlSessionFactoryBean sqlSessionFactory = new GeneralSqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource);
		sqlSessionFactory.setPlugins(new Interceptor[]{new GeneralPaginationInterceptor(null)});
		sqlSessionFactory.setTypeAliasesPackage("com.github.yuxiaobin.mybatis.gm.test.entity.persistent");
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setDefaultScriptingLanguage(GeneralMybatisXMLLanguageDriver.class);
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		configuration.setMapUnderscoreToCamelCase(true);
		sqlSessionFactory.setConfiguration(configuration);
		return sqlSessionFactory;
	}
	
	@Bean
	public GeneralMapper generalMapper(GeneralSqlSessionFactoryBean factoryBean) throws Exception{
		GeneralMapper generalMapper = new GeneralMapper();
		generalMapper.setSqlSessionFactory(factoryBean.getObject());
		return generalMapper;
	}
}
