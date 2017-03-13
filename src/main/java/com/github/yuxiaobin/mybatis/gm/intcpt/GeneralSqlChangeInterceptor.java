package com.github.yuxiaobin.mybatis.gm.intcpt;

import com.github.yuxiaobin.mybatis.gm.exceptions.SqlChangeException;

/**
 * By default, for mybatis plugins, only Interceptor for StatementHandler allows to inner change the executed sql.<BR>
 * <BR>
 * But the interceptor not allow plugin more than once, otherwise the later interceptor only can get the wrapped object.<BR>
 * cannot get sql and do inner change.
 * <BR>
 * This {@code GeneralSQLChangeInterceptor} allows multiple interceptors to change sql.<BR>
 * <BR>
 * <b>NOTE:</b><BR>
 * The {@code GeneralSqlChangeInterceptor} will intercept all the query sqls, so you need some config: like {@code SqlChangeInterceptorFlagHolder} which contains a {@link java.lang.ThreadLocal}.<BR>
 * In intercept() check the {@code flag} to determine if need to change the sql.<BR>
 * 
 * <BR>
 * NOTE: currently only support Select SQL as {@code GeneralSqlChangeInterceptor} only injected to {@code GeneralPaginationInterceptor}.
 * 
 * @since 1.8
 * @author yuxiaobin
 *
 */
public interface GeneralSqlChangeInterceptor {
	
	public String intercept(String originalSql) throws SqlChangeException;

}
