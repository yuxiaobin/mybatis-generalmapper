package com.github.yuxiaobin.mybatis.gm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.toolkit.StringUtils;
import com.baomidou.mybatisplus.toolkit.TableFieldInfo;
import com.baomidou.mybatisplus.toolkit.TableInfo;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;
import com.github.yuxiaobin.mybatis.gm.plus.GeneralEntitySubTypesHolder;


/**
 * 
 * <p>
 * This object is used to solve EntityWrapper.where/and/or... Parameter Type of Date. Mybatis-plus_v_1.5<BR>
 * <BR>
 * <BR>
 * Eg:<BR>
 * 		EntityWrapper ew = new EntityWrapper(entity);<BR>
 * 		ew.andNew("col1_date &gt; {0} and col1_date&lt;{1}", LocalDate.of(2017,2,10), LocalDate.of(2017,2,15));<BR>
 * <BR>
 * SQL:<BR>
 * 		select xx from tbl_entity where col1_date&gt;2017-2-10 and col1_date&lt;2017-2-15<BR>
 * <BR>
 * Question:<BR>
 * 		1) Mysql: no record will be found due to condition is false forever.(actual sql: col1_date&gt;2005 and col1_date&lt;2000) : do subtraction<BR>
 * 		2) Other db(oracle), sql will encounter error<BR>
 * <BR>
 * Solution:<BR>
 * 		1) just use: ew.andNew("col1_date &gt; {0} and col1_date&lt;{1}", LocalDate.of(2017,2,10), LocalDate.of(2017,2,15));<BR>
 * 			sqlSegment will be replaced to : col1_date &gt; #{GENVAL1} and col1_date&lt;#{GENVAL2}, <BR>
 * 			And GENVAL1,GENVAL2 will be passed to mybatis via Map.<BR>
 * 		2) OR try like this: ew.andNew("col1_date &gt; #{dateFrom} and col1_date&lt;#{dateTo}", LocalDate.of(2017,2,10), LocalDate.of(2017,2,15));<BR>
 * 		3) BUT {0} and #{value}, mixed use currently NOT supported.<BR>
 * </p>
 * 
 * 
 * 
 * 
 * @author Kelly Lake(179634696@qq.com)
 *
 */
public class GeneralEntityWrapper<T> extends EntityWrapper<T>{
	
	public static final String OPEN_TOKEN = "#{";
	public static final String CLOSE_TOKEN = "}";
	
	private static final String GENERAL_PARAMNAME = "GENVAL";
	
	private Map<String,Object> paramNameValuePairs = new HashMap<>(4);
	
	private AtomicInteger paramNameSeq = new AtomicInteger(0);

	private static final long serialVersionUID = 1L;

	
	public GeneralEntityWrapper() {
		super();
	}

	public GeneralEntityWrapper(T entity, String sqlSelect) {
		super(entity, sqlSelect);
	}

	public GeneralEntityWrapper(T entity) {
		super(entity);
	}

	/**
	 * Plus Injected Sql will use EntityWrapper.sqlSegment in sql script<BR>
	 * <BR>
	 * Refer to {@code AutoSqlInjector.sqlWhereEntityWrapper()}<BR>
	 * <BR>
	 * If ew.entity is an empty object, sql structure:<BR>
	 * select col1, col2 from tbl_entity #{sqlSegment}, sqlSegment should start with "WHERE";<BR>
	 * If ew.entity has property values, sql structure:<BR>
	 * select col1, col2 from tbl_entity where col1=#{propertyVal1} #{sqlSegment}, sqlSegment should start with "AND"<BR>
	 * 
	 * @since 1.6
	 */
	@Override
	public String getSqlSegment() {
		String sqlWhere = sql.toString();
		if (StringUtils.isEmpty(sqlWhere)) {
			return null;
		}
		sqlWhere = checkFieldValueNotNull()? sqlWhere.replaceFirst("WHERE", "AND") : sqlWhere;
		return sqlWhere;
	}
	
	/**
	 * Get corresponding class and check property values NOT null.
	 * 
	 * @since 1.6
	 * @return
	 */
	public boolean checkFieldValueNotNull(){
		Class<?> realEntityClazz = GeneralEntitySubTypesHolder.get(entity.getClass());
		if(realEntityClazz==null){
			realEntityClazz = entity.getClass();
		}
		TableInfo tableInfo = TableInfoHelper.getTableInfo(realEntityClazz);
		if (null == tableInfo) {
			return false;
		}
		List<TableFieldInfo> fieldList = tableInfo.getFieldList();
		for (TableFieldInfo tableFieldInfo : fieldList) {
			Object val = ReflectionKit.getMethodValue(realEntityClazz, entity, tableFieldInfo.getProperty());
			if(null != val){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @since 1.6
	 * @return
	 */
	public Map<String,Object> getParamNameValuePairs(){
		return paramNameValuePairs;
	}

	/**
	 * Format SQL for methods: EntityWrapper.where/and/or...("name={0}", value);<BR>
	 * <BR>
	 * ew.where("sample_name={0}", "haha").and("sample_age &gt;{0} and sample_age&lt;{1}", 18, 30);<BR>
	 * OR<BR>
	 * ew.where("sample_name=#{name}", "haha").and("sample_age &gt;#{ageFrom} and sample_age&lt;#{ageTo}", 18, 30);<BR>
	 * BUT<BR>
	 * {0} and #{value} cannot be mixed used.<BR>
	 * eg:<BR>
	 * ew.and("sample_age &gt;{0} and sample_age&lt;#{ageTo}", 18, 30);//not support<BR>
	 * 
	 * 
	 * @param need true
	 * @param sqlStr normally like: "column_name={0}"
	 * @param params
	 * 
	 * @since 1.6
	 */
	@Override
	protected String formatSqlIfNeed(boolean need, String sqlStr, Object... params) {
		if (!need || StringUtils.isEmpty(sqlStr)) {
			return null;
		}
		GeneralTokenHandler handler = new GeneralTokenHandler();
		GenericTokenParser parser = new GenericTokenParser(OPEN_TOKEN, CLOSE_TOKEN, handler);
		parser.parse(sqlStr);
		if(handler.hasParam()){
			List<String> paramNames = handler.getParamNames();
			if(paramNames!=null && !paramNames.isEmpty()){
				int parmNameSize = paramNames.size();
				int parmArgSize = params==null?0:params.length;
				if(parmNameSize>parmArgSize){
					for(int i=0;i<parmArgSize;++i){
						paramNameValuePairs.put(paramNames.get(i), params[i]);
					}
				}else{
					for(int i=0;i<parmNameSize;++i){
						paramNameValuePairs.put(paramNames.get(i), params[i]);
					}
				}
			}
		}else{
			if(params!=null && params.length!=0){
				int size = params.length;
				for(int i=0;i<size;++i){
					String genParamName = GENERAL_PARAMNAME+paramNameSeq.incrementAndGet();
					sqlStr = sqlStr.replace("{"+i+"}", OPEN_TOKEN+genParamName+CLOSE_TOKEN);
					paramNameValuePairs.put(genParamName, params[i]);
				}
			}
		}
		// TODO 使用jsqlparser来解析表达式，获取属性名/字段名，再通过tableInfo缓存获得对应的column_name, @2017-2-8
		return sqlStr;
	}

	/**
	 * ew.where("col_1&gt;{0} and col_1&lt;{1}", Object val1, Object val2);<BR>
	 * <BR>
	 * <BR>
	 * eg: <BR>
	 * ew.where("name='Bob'").where("id=#{value1}",123).where("age&gt;#{value2}", 18); <BR>
	 * OR <BR>
	 * ew.where("name='Bob'").where("id={0}",123).where("age&gt;{1}", 18); <BR>
	 * sql:<BR>
	 * where (name='Bob' and id=#{value1} and age&gt;#{value2})<BR>
	 * The values of "value1" and "value2" will be passed to mybatis via Map param.<BR>
	 *<BR>
	 *<BR>
	 * NOTE: NOT support mixed usage for {0} and #{value1}.<BR>
	 *<BR>
	 *ew.where("id={0}",123).where("age&gt;#{age}", 18) currently NOT SUPPORT.<BR>
	 * 
	 * @param sqlWhere sql segment: "col_1&gt;{0} AND col_1&lt;{1}" or "col_1&gt;#{val1} AND col_1&lt;#{val2}"
	 * @param params 
	 * @since 1.6 just show how to deal
	 */
	@Override
	public EntityWrapper<T> where(String sqlWhere, Object... params) {
		return super.where(sqlWhere, params);
	}

	/**
	 * Code:<BR>
	 * ew.and("col1=18").and("col2={0}", 20)<BR>
	 * SQL:<BR>
	 * AND col1=18 AND col2=20<BR>
	 */
	@Override
	public EntityWrapper<T> and(String sqlAnd, Object... params) {
		return super.and(sqlAnd, params);
	}

	/**
	 * Use () to separate sql segment.<BR>
	 * <BR>
	 * Code:<BR>
	 * ew.and("col1=18").andNew("col2&gt;2 or col2&lt;10")<BR>
	 * SQL:<BR>
	 * AND col1=18 AND ( col2&gt;2 or col2&lt;10 )<BR>
	 */
	@Override
	public EntityWrapper<T> andNew(String sqlAnd, Object... params) {
		return super.andNew(sqlAnd, params);
	}

	/**
	 * Code:<BR>
	 * ew.or("col1=18").or("col2={0}", 20)<BR>
	 * SQL:<BR>
	 * OR col1=18 OR col2=20<BR>
	 */
	@Override
	public EntityWrapper<T> or(String sqlOr, Object... params) {
		return super.or(sqlOr, params);
	}

	/**
	 * Code:<BR>
	 * ew.or("col1=18").orNew("col2={0} and col3={0}", 20)<BR>
	 * SQL:<BR>
	 * OR col1=18 OR ( col2=20 and col3=20 )<BR>
	 */
	@Override
	public EntityWrapper<T> orNew(String sqlOr, Object... params) {
		return super.orNew(sqlOr, params);
	}

	/**
	 * Code:<BR>
	 * eg: ew.groupBy("id,name").having("id={0}",22).and("password is not null")<BR>
	 * SQL:<BR>
	 * group by id, name having id=22 and password is not null<BR>
	 */
	@Override
	public EntityWrapper<T> having(String sqlHaving, Object... params) {
		return super.having(sqlHaving, params);
	}

	/**
	 * @deprecated instead use <code>where(String sqlWhere, Object... params)</code>,<code>and(String sqlAnd, Object... params)</code>...
	 */
	@Deprecated
	@Override
	public EntityWrapper<T> addFilter(String sqlWhere, Object... params) {
		return super.addFilter(sqlWhere, params);
	}

	/**
	 * @deprecated instead use <code>where(String sqlWhere, Object... params)</code>,<code>and(String sqlAnd, Object... params)</code>...
	 */
	@Deprecated
	@Override
	public EntityWrapper<T> addFilterIfNeed(boolean need, String sqlWhere, Object... params) {
		return super.addFilterIfNeed(need, sqlWhere, params);
	}
	
}

/**
 * 
 *@since 1.6
 */
class GeneralTokenHandler implements TokenHandler{

	private List<String> paramNames = new ArrayList<>(4);
	@Override
	public String handleToken(String content) {
		paramNames.add(content);
		return content;
	}

	public List<String> getParamNames() {
		return paramNames;
	}

	public boolean hasParam(){
		return !paramNames.isEmpty();
	}
	
}