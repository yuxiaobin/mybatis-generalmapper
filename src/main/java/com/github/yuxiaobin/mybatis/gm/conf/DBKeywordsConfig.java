package com.github.yuxiaobin.mybatis.gm.conf;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(value="classpath:keywords.properties", ignoreResourceNotFound=true)
public class DBKeywordsConfig {

	@Autowired
    Environment env;
	
	@Bean
	public DBKeywords getDBKeywords(){
		List<String> keywords = new ArrayList<>();
		String oracleKeys = env.getProperty("oracle.keywords");
		if(oracleKeys!=null && !oracleKeys.trim().isEmpty()){
			String[] array = oracleKeys.split(",");
			for(String key:array){
				if(!key.trim().isEmpty()){
					keywords.add(key.toUpperCase());
				}
			}
		}
		String mysqlKeys = env.getProperty("mysql.keywords");
		if(mysqlKeys!=null && !mysqlKeys.trim().isEmpty()){
			String[] array = mysqlKeys.split(",");
			for(String key:array){
				if(!key.trim().isEmpty()){
					keywords.add(key.toUpperCase());
				}
			}
		}
		DBKeywords keys = new DBKeywords();
		keys.setKeywords(keywords);
		return keys;
	}
	
	public static class DBKeywords{
		private List<String> keywords;

		public List<String> getKeywords() {
			return keywords;
		}

		public void setKeywords(List<String> keywords) {
			this.keywords = keywords;
		}
	}
}
