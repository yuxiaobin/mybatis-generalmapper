package com.github.yuxiaobin.mybatis.gm.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import com.baomidou.mybatisplus.MybatisConfiguration;
import com.baomidou.mybatisplus.mapper.AutoSqlInjector;
import com.baomidou.mybatisplus.mapper.SqlMethod;
import com.baomidou.mybatisplus.toolkit.TableFieldInfo;
import com.baomidou.mybatisplus.toolkit.TableInfo;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;

/**
 * Single Table CURD SQL Injector
 * 
 * @see com.baomidou.mybatisplus.mapper.AutoSqlInjector
 * @author Kelly Lake(179634696@qq.com)
 */
public class GeneralMapperSqlInjector extends AutoSqlInjector {
	
	protected static final Logger logger = Logger.getLogger("GeneralMapperSqlInjector");
	
	private final List<String> keywords = new ArrayList<>();
	
	private String keyWordWrapper = null;

    /**
     * CRUD sql inject
     */
    @Override
    public void inject(Configuration configuration, MapperBuilderAssistant builderAssistant, Class<?> mapperClass,
                       Class<?> modelClass, TableInfo table) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
        this.languageDriver = configuration.getDefaultScriptingLanuageInstance();
        if(configuration instanceof MybatisConfiguration){
        	this.dbType = MybatisConfiguration.DB_TYPE;
        }
		switch (this.dbType) {
		case MYSQL:
			keyWordWrapper = "'";
			keywords.add("ASC");
			keywords.add("DESC");
			break;
		case ORACLE:
			keyWordWrapper = "\"";
			keywords.add("ASC");
			keywords.add("AS");
			keywords.add("CHAR");
			keywords.add("COLUMN");
			keywords.add("COMMENT");
			keywords.add("DATE");
			keywords.add("DECIMAL");
			keywords.add("DELETE");
			keywords.add("DESC");
			keywords.add("FOR");
			keywords.add("GROUP");
			keywords.add("LEVEL");
			break;
		}
        
        String modelClassName = modelClass.getName();
        String pattern = "^org.(apache|spring|hibernate).*";
        /*
         * System class/third part class(apache,spring,hibernate), inject ignore
         * @Since 1.7
         */
        if(modelClassName.startsWith("java") || modelClassName.matches(pattern)){
        	return;
        }
        table = TableInfoHelper.initTableInfo(modelClass);
        /**
         * 没有指定主键，默认方法不能使用,
         * 如果是中间表，可以配置一个字段为@TableId，这样除了ById的方法不能用，其他都可以用.
         * PersistentEntity should contains {@code @TableId}, which is the table PK.
         * NOTE: if middle table don't have PK, can set one property {@code @TableId}, 
         * 		then CRUD SQL can also be injected, but **ById() method will not work properly.
         */
        if (table!=null && table.getKeyProperty() != null) {
            /* 插入 Insert SQL */
            this.injectInsertOneSql(false, mapperClass, modelClass, table);
            this.injectInsertOneSql(true, mapperClass, modelClass, table);
            this.injectInsertBatchSql(mapperClass, modelClass, table);
            /* 删除  Delete SQL*/
            this.injectDeleteSelectiveSql(mapperClass, modelClass, table);
            this.injectDeleteByMapSql(mapperClass, table);
            this.injectDeleteSql(false, mapperClass, modelClass, table);
            this.injectDeleteSql(true, mapperClass, modelClass, table);
            this.injectDeleteByEWSql(mapperClass, modelClass,  table);
            /* 修改  Update SQL*/
            this.injectUpdateByIdSql(false, mapperClass, modelClass, table);
            this.injectUpdateByIdSql(true, mapperClass, modelClass, table);
            this.injectUpdateSql(false, mapperClass, modelClass, table);
            this.injectUpdateSql(true, mapperClass, modelClass, table);
            this.injectUpdateBatchById(mapperClass, modelClass, table);
			/* 查询  Select SQL*/
            this.injectSelectSql(false, mapperClass, modelClass, table);
            this.injectSelectSql(true, mapperClass, modelClass, table);
            this.injectSelectByMapSql(mapperClass, modelClass, table);
            this.injectSelectOneSql(mapperClass, modelClass, table);
            this.injectSelectCountSql(mapperClass, modelClass, table);
            this.injectSelectCountByEWSql(SqlMethod.SELECT_COUNT_EW, mapperClass, modelClass, table);
            this.injectSelectListSql(SqlMethod.SELECT_LIST, mapperClass, modelClass, table);
            this.injectSelectListSql(SqlMethod.SELECT_PAGE, mapperClass, modelClass, table);
        } else {
            logger.warning(String.format("%s ,Not found @TableId annotation, cannot use mybatis-plus curd method.",
					modelClass.toString()));
        }
    }
    
    /**
     * 删除满足条件的记录<BR>
     * Delete by EntityWrapper<BR>
     * 
     * 条件：EntityWrapper<BR>
     * 
     * @param mapperClass
     * @param table
     */
    protected void injectDeleteByEWSql(Class<?> mapperClass, Class<?> modelClass, TableInfo table) {
    	ExtraSqlMethod sqlMethod = ExtraSqlMethod.DELETE_BY_EW;
		String sql = String.format(sqlMethod.getSql(), table.getTableName(), sqlWhereEntityWrapper(table));
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);
		this.addDeleteMappedStatement(mapperClass, sqlMethod.getMethod(), sqlSource);
	}
    
    //TODO:现在insertBatch只支持一种数据库，如果动态切，会出错！！！2017-3-15
    
    public static enum ExtraSqlMethod{
    	
    	DELETE_BY_EW("deleteByEw", "删除满足条件的记录", "<script>DELETE FROM %s %s</script>");
    	
    	private final String method;
    	
    	private final String desc;

    	private final String sql;

    	ExtraSqlMethod( final String method, final String desc, final String sql ) {
    		this.method = method;
    		this.desc = desc;
    		this.sql = sql;
    	}
		public String getMethod() {
			return method;
		}
		public String getDesc() {
			return desc;
		}
		public String getSql() {
			return sql;
		}
    	
    }

	@Override
	protected String sqlSelectColumns(TableInfo table, boolean entityWrapper) {
		StringBuilder columns = new StringBuilder();
		if (null != table.getResultMap()) {
			/*
			 * 存在 resultMap 映射返回
			 */
			if (entityWrapper) {
				columns.append("<choose><when test=\"ew != null and ew.sqlSelect != null\">${ew.sqlSelect}</when><otherwise>");
			}
			columns.append("*");
			if (entityWrapper) {
				columns.append("</otherwise></choose>");
			}
		} else {
			/*
			 * 普通查询
			 */
			if (entityWrapper) {
				columns.append("<choose><when test=\"ew != null and ew.sqlSelect != null\">${ew.sqlSelect}</when><otherwise>");
			}
			if (table.isKeyRelated()) {
				columns.append(table.getKeyColumn()).append(" AS ").append(convertKeyWords4Property(table.getKeyProperty()));
			} else {
				columns.append(convertKeyWords4Property(table.getKeyProperty()));
			}
			List<TableFieldInfo> fieldList = table.getFieldList();
			for (TableFieldInfo fieldInfo : fieldList) {
				columns.append(",").append(fieldInfo.getColumn());
				if (fieldInfo.isRelated()) {
					columns.append(" AS ").append(convertKeyWords4Property(fieldInfo.getProperty()));
				}
			}
			if (entityWrapper) {
				columns.append("</otherwise></choose>");
			}
		}

		/*
		 * 返回所有查询字段内容
		 */
		return columns.toString();
	}
    
	public String convertKeyWords4Property(String property){
		if(keywords.contains(property.toUpperCase())){
			return keyWordWrapper + property+keyWordWrapper;
		}else{
			return property;
		}
	}
	
	public void addKeyWords(Collection<String> words){
		keywords.addAll(words);
	}
    
	public void addKeyWord(String... words){
		if(words!=null && words.length!=0){
			for(String s:words){
				keywords.add(s);
			}
		}
	}
    
}