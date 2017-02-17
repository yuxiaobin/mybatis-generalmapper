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
 * This object is used to solve EntityWrapper.where/and/or... Parameter Type of Date. Mybatis-plus_v_1.5
 * </br>
 * </br>
 * Eg:</br>
 * 		EntityWrapper ew = new EntityWrapper(entity);</br>
 * 		ew.andNew("col1_date > {0} and col1_date<{1}", LocalDate.of(2017,2,10), LocalDate.of(2017,2,15));</br>
 * </br>
 * SQL:</br>
 * 		select xx from tbl_entity where col1_date>2017-2-10 and col1_date<2017-2-15</br>
 * </br>
 * Question:</br>
 * 		1) Mysql: no record will be found due to condition is false forever.(actual sql: col1_date>2005 and col1_date<2000) : <b>do subtraction</b></br>
 * 		2) Other db(oracle), sql will encounter error</br>
 * </br>
 * Solution:</br>
 * 		1) just use: ew.andNew("col1_date > {0} and col1_date<{1}", LocalDate.of(2017,2,10), LocalDate.of(2017,2,15));</br>
 * 			sqlSegment will be replaced to : col1_date > #{GENVAL1} and col1_date<#{GENVAL2}, </br>
 * 			And GENVAL1,GENVAL2 will be passed to mybatis via Map.</br>
 * 		2) OR try like this: ew.andNew("col1_date > #{dateFrom} and col1_date<#{dateTo}", LocalDate.of(2017,2,10), LocalDate.of(2017,2,15));</br>
 * 		3) <b>BUT</b> {0} & #{value}, mixed use currently <b>NOT</b> supported.
 * </p>
 * 
 * 
 * 
 * 
 * @author Kelly Lake(179634696@qq.com)
 *
 * @param <T>
 */
public class GeneralEntityWrapper<T> extends EntityWrapper<T>{
	
	public static final String OPEN_TOKEN = "#{";
	public static final String CLOSE_TOKEN = "}";
	
	private static final String GENERAL_PARAMNAME = "GENVAL";
	
	private Map<String,Object> paramNameValuePairs = new HashMap<>(4);
	
	private AtomicInteger paramNameSeq = new AtomicInteger(0);

	private static final long serialVersionUID = 1L;

	/**
	 * Plus Injected Sql will use EntityWrapper.sqlSegment in sql script</br>
	 * </br>
	 * Refer to {@code AutoSqlInjector.sqlWhereEntityWrapper()}</br>
	 * </br>
	 * If ew.entity is an empty object, sql structure:</br>
	 * select col1, col2 from tbl_entity #{sqlSegment}, sqlSegment should start with "WHERE";</br>
	 * If ew.entity has property values, sql structure:</br>
	 * select col1, col2 from tbl_entity where col1=#{propertyVal1} #{sqlSegment}, sqlSegment should start with "AND"</br>
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
	protected boolean checkFieldValueNotNull(){
		Class<?> realEntityClazz = GeneralEntitySubTypesHolder.get(entity.getClass());
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
	 * Format SQL for methods: EntityWrapper.where/and/or...("name={0}", value);</br>
	 * </br>
	 * ew.where("sample_name={0}", "haha").and("sample_age >{0} and sample_age<{1}", 18, 30);</br>
	 * OR</br>
	 * ew.where("sample_name=#{name}", "haha").and("sample_age >#{ageFrom} and sample_age<#{ageTo}", 18, 30);</br>
	 * BUT</br>
	 * {0} & #{value} cannot be mixed used.</br>
	 * eg:</br>
	 * ew.and("sample_age >{0} and sample_age<#{ageTo}", 18, 30);//not support
	 * </br>
	 * 
	 * @param need true
	 * @param sqlStr normally like: "column_name={0}"
	 * @param parms param values
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
				String[] paramNames = new String[size];
				for(int i=0;i<size;++i){
					String genParamName = GENERAL_PARAMNAME+paramNameSeq.incrementAndGet();
					paramNames[i] = genParamName;
					sqlStr = sqlStr.replace("{"+i+"}", OPEN_TOKEN+genParamName+CLOSE_TOKEN);
					paramNameValuePairs.put(genParamName, params[i]);
				}
			}
		}
		// TODO 使用jsqlparser来解析表达式，获取属性名/字段名，再通过tableInfo缓存获得对应的column_name, @2017-2-8
		return sqlStr;
	}

	/**
	 * ew.where("col_1>{0} and col_1<{1}", Object val1, Object val2);
	 * </br>
	 * </br>
	 * eg:</br> 
	 * ew.where("name='Bob'").where("id=#{value1}",123).where("age>#{value2}", 18);</br> 
	 * OR </br>
	 * ew.where("name='Bob'").where("id={0}",123).where("age>{1}", 18);</br> 
	 * sql:</br>
	 * where (name='Bob' and id=#{value1} and age>#{value2})</br>
	 * The values of "value1"&"value2" will be passed to mybatis via Map param.</br>
	 *</br>
	 *<b>
	 * NOTE: NOT support mixed usage for {0} & #{value1}.</br>
	 *</b>
	 *ew.where("id=<b>{0}</b>",123).where("age><b>#{age}</b>", 18) currently <b>NOT SUPPORT</b>.
	 * 
	 * @param sqlWhere sql segment: "col_1>{0} AND col_1<{1}" or "col_1>#{val1} AND col_1<#{val2}"
	 * @param params 
	 * @since 1.6 just show how to deal
	 */
	@Override
	public EntityWrapper<T> where(String sqlWhere, Object... params) {
		return super.where(sqlWhere, params);
	}

	/**
	 * Code:</br>
	 * ew.and("col1=18").and("col2={0}", 20)</br>
	 * SQL:</br>
	 * AND col1=18 AND col2=20</br>
	 */
	@Override
	public EntityWrapper<T> and(String sqlAnd, Object... params) {
		return super.and(sqlAnd, params);
	}

	/**
	 * Use () to separate sql segment.</br>
	 * </br>
	 * Code:</br>
	 * ew.and("col1=18").andNew("col2>2 or col2<10")</br>
	 * SQL:</br>
	 * AND col1=18 AND ( col2>2 or col2<10 )</br>
	 */
	@Override
	public EntityWrapper<T> andNew(String sqlAnd, Object... params) {
		return super.andNew(sqlAnd, params);
	}

	/**
	 * Code:</br>
	 * ew.or("col1=18").or("col2={0}", 20)</br>
	 * SQL:</br>
	 * OR col1=18 OR col2=20</br>
	 */
	@Override
	public EntityWrapper<T> or(String sqlOr, Object... params) {
		return super.or(sqlOr, params);
	}

	/**
	 * Code:</br>
	 * ew.or("col1=18").orNew("col2={0} and col3={0}", 20)</br>
	 * SQL:</br>
	 * OR col1=18 OR ( col2=20 and col3=20 )</br>
	 */
	@Override
	public EntityWrapper<T> orNew(String sqlOr, Object... params) {
		return super.orNew(sqlOr, params);
	}

	/**
	 * Code:</br>
	 * eg: ew.groupBy("id,name").having("id={0}",22).and("password is not null")</br>
	 * SQL:</br>
	 * group by id, name having id=22 and password is not null</br>
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