package com.github.yuxiaobin.mybatis.gm.test.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

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
		sqlSessionFactory.setTypeAliasesPackage("com.github.yuxiaobin.mybatis.gm.test.entity.persistent");
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setDefaultScriptingLanguage(GeneralMybatisXMLLanguageDriver.class);

		configuration.setJdbcTypeForNull(JdbcType.NULL);
		configuration.setMapUnderscoreToCamelCase(true);
		sqlSessionFactory.setConfiguration(configuration);
		sqlSessionFactory.setPlugins(new Interceptor[]{
				new GeneralPaginationInterceptor(null),
		});

		String[] mapperLocations = new String[]{"mapper/*.xml"};
		ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
		List<Resource> resources = new ArrayList<>();
		for (String mapperLocation : mapperLocations) {
			try {
				Resource[] mappers = resourceResolver.getResources(mapperLocation);
				resources.addAll(Arrays.asList(mappers));
			} catch (IOException e) {
				// ignore
			}
		}
		sqlSessionFactory.setMapperLocations(resources.toArray(new Resource[resources.size()]));
		return sqlSessionFactory;
	}
	
	@Bean
	public GeneralMapper generalMapper(GeneralSqlSessionFactoryBean factoryBean) throws Exception{
		GeneralMapper generalMapper = new GeneralMapper();
		generalMapper.setSqlSessionFactory(factoryBean.getObject());
		return generalMapper;
	}
}
