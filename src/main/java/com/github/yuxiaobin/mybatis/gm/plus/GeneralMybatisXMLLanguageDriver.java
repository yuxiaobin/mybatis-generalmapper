package com.github.yuxiaobin.mybatis.gm.plus;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

/**
 * 自定义XMLLanguageDriver
 * 
 * @author yuxiaobin
 *
 */
public class GeneralMybatisXMLLanguageDriver extends XMLLanguageDriver {

	@Override
	public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject,
			BoundSql boundSql) {
		return new GeneralMybatisParameterHandler(mappedStatement, parameterObject, boundSql);
	}

}
