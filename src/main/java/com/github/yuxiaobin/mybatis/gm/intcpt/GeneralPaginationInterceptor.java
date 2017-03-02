package com.github.yuxiaobin.mybatis.gm.intcpt;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.RowBounds;

import com.baomidou.mybatisplus.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.plugins.pagination.IDialect;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.github.yuxiaobin.mybatis.gm.exceptions.SqlChangeException;
import com.github.yuxiaobin.mybatis.gm.utils.GeneralJdbcReflectionUtil;

/**
 * For multiple Interceptors which need to change sql, StatementHandler won't work for second interceptor and the rest.<BR>
 * <BR>
 * Solution:<BR>
 * use {@link GeneralSqlChangeInterceptor}, currently only support passed in String and output String.<BR>
 * Any query will be intercepted by {@code GeneralPaginationInterceptor}, and for sql change interceptors will be injected to {@code interceptors}.<BR>
 * <BR>
 * Pagination sql will be detected by the {@link java.sql.Connection}. Refer to {@link GeneralJdbcReflectionUtil} and {@link DialectFactory}
 * 
 * @see com.baomidou.mybatisplus.plugins.PaginationInterceptor
 * @since 1.8
 * @author Kellylake(179634696@qq.com)
 *
 */
@Intercepts(
	{ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) }
)
public class GeneralPaginationInterceptor implements Interceptor {
	
	private static final Log LOGGER = LogFactory.getLog(GeneralPaginationInterceptor.class);
	
	private final GeneralSqlChangeInterceptor[] interceptors;
	
	public GeneralPaginationInterceptor(GeneralSqlChangeInterceptor[] interceptors){
		this.interceptors = interceptors;
	}
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object target = invocation.getTarget();
		if (target instanceof StatementHandler) {
			StatementHandler statementHandler = (StatementHandler) target;
			MetaObject metaStatementHandler = SystemMetaObject.forObject(statementHandler);
			RowBounds rowBounds = (RowBounds) metaStatementHandler.getValue("delegate.rowBounds");
			BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
			String originalSql = (String) boundSql.getSql();
			if (rowBounds != null && rowBounds != RowBounds.DEFAULT) {
				Connection conn = (Connection) invocation.getArgs()[0];
				String dbUrl = conn.getMetaData().getURL();
				String dialectType = GeneralJdbcReflectionUtil.getDbType(dbUrl).getDb();
				IDialect dialect = DialectFactory.getDialectByDbtype(dialectType);
				if(rowBounds instanceof Pagination){//avoid use empty constructor to init Page/Pagination object.
					Pagination page = (Pagination)rowBounds;
					originalSql = dialect.buildPaginationSql(originalSql, page.getOffsetCurrent(), page.getSize());
				}else{
					originalSql = dialect.buildPaginationSql(originalSql, rowBounds.getOffset(), rowBounds.getLimit());
				}
				metaStatementHandler.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
				metaStatementHandler.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
			}
			if(interceptors!=null && interceptors.length!=0){
				String changeSql = null;
				for(GeneralSqlChangeInterceptor intcpt : interceptors){
					try{
						changeSql = intcpt.intercept(originalSql);
						if(changeSql!=null){
							originalSql = changeSql;
						}
					}catch(SqlChangeException e){
						LOGGER.error("GeneralSqlChangeInterceptor.intercept() error", e);
					}
				}
			}
			metaStatementHandler.setValue("delegate.boundSql.sql", originalSql);
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	@Override
	public void setProperties(Properties properties) {

	}

}
