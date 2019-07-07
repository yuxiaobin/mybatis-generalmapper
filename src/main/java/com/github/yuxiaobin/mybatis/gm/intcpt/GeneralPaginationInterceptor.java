package com.github.yuxiaobin.mybatis.gm.intcpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.RowBounds;

import com.baomidou.mybatisplus.MybatisDefaultParameterHandler;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.plugins.pagination.IDialect;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.baomidou.mybatisplus.plugins.pagination.dialects.MySqlDialect;
import com.github.yuxiaobin.mybatis.gm.exceptions.SqlChangeException;
import com.github.yuxiaobin.mybatis.gm.plus.ISqlParser;
import com.github.yuxiaobin.mybatis.gm.plus.SqlInfo;
import com.github.yuxiaobin.mybatis.gm.utils.GeneralJdbcReflectionUtil;
import com.github.yuxiaobin.mybatis.gm.utils.MybatisPluginUtil;
import com.github.yuxiaobin.mybatis.gm.utils.SqlUtils;

/**
 * Mybatis Physical Pagination Intercetptor.<BR>
 * <BR>
 * Use {@link GeneralSqlChangeInterceptor} to change <b>Query SQL</b> before executed, currently only support passed in String and output String.<BR>
 * Any query will be intercepted by {@code GeneralPaginationInterceptor}, and for sql change interceptors will be injected to {@code interceptors}.<BR>
 * <BR>
 * Pagination sql will be detected by the {@link java.sql.Connection}. Refer to {@link GeneralJdbcReflectionUtil} and {@link DialectFactory}
 *
 * @author Kellylake(179634696 @ qq.com)
 * @see com.baomidou.mybatisplus.plugins.PaginationInterceptor
 * @since 1.8
 */
@Intercepts(
        {@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})}
)
public class GeneralPaginationInterceptor implements Interceptor {

    private static final Log LOGGER = LogFactory.getLog(GeneralPaginationInterceptor.class);

    private final GeneralSqlChangeInterceptor[] interceptors;

    /**
     * COUNT SQL 解析
     */
    private ISqlParser sqlParser;
    /**
     * 溢出总页数，设置第一页
     */
    private boolean overflowCurrent = false;

    public GeneralPaginationInterceptor(GeneralSqlChangeInterceptor[] interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof StatementHandler) {
            StatementHandler statementHandler = (StatementHandler) MybatisPluginUtil.getRealTarget(target);
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            String originalSql = (String) boundSql.getSql();
            if (interceptors != null && interceptors.length != 0) {
                String changeSql = null;
                for (GeneralSqlChangeInterceptor intcpt : interceptors) {
                    try {
                        changeSql = intcpt.intercept(originalSql);
                        if (changeSql != null) {
                            originalSql = changeSql;
                        }
                    } catch (SqlChangeException e) {
                        LOGGER.error("GeneralSqlChangeInterceptor.intercept() error", e);
                    }
                }
            }
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
                metaObject.setValue("delegate.boundSql.sql", originalSql);
                return invocation.proceed();
            }
            RowBounds rowBounds = (RowBounds) metaObject.getValue("delegate.rowBounds");
            if (rowBounds != null && rowBounds != RowBounds.DEFAULT) {
                Connection conn = (Connection) invocation.getArgs()[0];
                String dbUrl = conn.getMetaData().getURL();
                String dialectType = GeneralJdbcReflectionUtil.getDbType(dbUrl).getDb();

                IDialect dialect = DialectFactory.getDialectByDbtype(dialectType);
                if (dialect == null) {
                    dialect = new MySqlDialect();
                }
                boolean isSearchCount = false;
                if (rowBounds instanceof Pagination) {
                    Pagination page = (Pagination) rowBounds;
                    isSearchCount = page.isSearchCount();
                    if (isSearchCount) {
                        SqlInfo sqlInfo = SqlUtils.getOptimizeCountSql(true, sqlParser, originalSql);
                        this.queryTotal(overflowCurrent, sqlInfo.getSql(), mappedStatement, boundSql, page, conn);
                        if (page.getTotal() <= 0) {
                            return invocation.proceed();
                        }
                    }
                    originalSql = dialect.buildPaginationSql(originalSql, page.getOffsetCurrent(), page.getSize());
                } else {
                    originalSql = dialect.buildPaginationSql(originalSql, rowBounds.getOffset(), rowBounds.getLimit());
                }
                metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
                metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
            }
            metaObject.setValue("delegate.boundSql.sql", originalSql);
        }
        return invocation.proceed();
    }

    /**
     * 查询总记录条数
     *
     * @param sql             count sql
     * @param mappedStatement MappedStatement
     * @param boundSql        BoundSql
     * @param page            IPage
     * @param connection      Connection
     */
    protected void queryTotal(boolean overflowCurrent, String sql, MappedStatement mappedStatement,
                              BoundSql boundSql, Pagination page, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DefaultParameterHandler parameterHandler = new MybatisDefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
            parameterHandler.setParameters(statement);
            int total = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    total = resultSet.getInt(1);
                }
            }
            page.setTotal(total);
            /*
             * 溢出总页数，设置第一页
             */
            long pages = page.getPages();
            if (overflowCurrent && page.getCurrent() > pages) {
                // 设置为第一条
                page.setCurrent(1);
            }
        } catch (Exception e) {
            throw new MybatisPlusException("Error: Method queryTotal execution error of sql ", e);
        }
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
