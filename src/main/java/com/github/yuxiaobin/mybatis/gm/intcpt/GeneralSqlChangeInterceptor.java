package com.github.yuxiaobin.mybatis.gm.intcpt;

import com.github.yuxiaobin.mybatis.gm.exceptions.SqlChangeException;

/**
 * By default, for mybatis plugins, only Interceptor for StatementHandler allows to inner change the executed sql.<BR>
 * <BR>
 * But the interceptor not allow plugin more than once, otherwise the later interceptor only can get the wrapped object.<BR>
 * cannot get sql and do inner change.
 * <BR>
 * This {@code GeneralSQLChangeInterceptor} allows multiple interceptors to change sql.<BR>
 * 
 * NOTE: currently only support Select SQL.
 * 
 * @since 1.8
 * @author yuxiaobin
 *
 */
public interface GeneralSqlChangeInterceptor {
	
	public String intercept(String originalSql) throws SqlChangeException;

}
