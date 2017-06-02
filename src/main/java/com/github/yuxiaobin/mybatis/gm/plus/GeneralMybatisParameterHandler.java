package com.github.yuxiaobin.mybatis.gm.plus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;

import com.baomidou.mybatisplus.MybatisConfiguration;
import com.baomidou.mybatisplus.annotations.IdType;
import com.baomidou.mybatisplus.mapper.IMetaObjectHandler;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.baomidou.mybatisplus.toolkit.TableInfo;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;

/**
 * <p>
 * This Class is used to populate Primary Key for INSERT method for {@code IdType.UUID} /{@code IdType.ID_WORKER}.
 * </p>
 * 
 * 
 * @author yuxiaobin
 *
 */
public class GeneralMybatisParameterHandler extends DefaultParameterHandler {

	public GeneralMybatisParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
		super(mappedStatement, processBatchGeneral(mappedStatement,parameterObject), boundSql);
	}

	protected static Object processBatchGeneral(MappedStatement ms, Object parameterObject) {
		if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
			/**
			 * 只处理插入操作
			 */
			Collection<Object> parameters = getParameters(parameterObject);
			if (null != parameters) {
				List<Object> objList = new ArrayList<Object>();
				for (Object parameter : parameters) {
					Class<?> entityClazz = GeneralEntitySubTypesHolder.get(parameter.getClass());
					if(entityClazz==null){
						entityClazz = parameter.getClass();
					}
					TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClazz);
					if (null != tableInfo) {
						objList.add(populateKeys(tableInfo, ms, parameter));
					} else {
						/*
						 * 非表映射类不处理
						 */
						objList.add(parameter);
					}
				}
				return objList;
			} else {
				/**
				 * To resolve insert entityVO > pk NOT auto filled issue
				 */
				Class<?> entityClazz = GeneralEntitySubTypesHolder.get(parameterObject.getClass());
				if(entityClazz==null){
					entityClazz = parameterObject.getClass();
				}
				TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClazz);
				return populateKeys(tableInfo, ms, parameterObject);
			}
		}
		return parameterObject;
	}
	
	
	/**
	 * <p>
	 * 处理正常批量插入逻辑
	 * </p>
	 * <p>
	 * org.apache.ibatis.session.defaults.DefaultSqlSession$StrictMap 该类方法
	 * wrapCollection 实现 StrictMap 封装逻辑
	 * </p>
	 * 
	 * @param parameter
	 *            插入数据库对象
	 * @return Collection
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static Collection<Object> getParameters(Object parameter) {
		Collection<Object> parameters = null;
		if (parameter instanceof Collection) {
			parameters = (Collection) parameter;
		} else if (parameter instanceof Map) {
			Map parameterMap = (Map) parameter;
			if (parameterMap.containsKey("collection")) {
				parameters = (Collection) parameterMap.get("collection");
			} else if (parameterMap.containsKey("list")) {
				parameters = (List) parameterMap.get("list");
			} else if (parameterMap.containsKey("array")) {
				parameters = Arrays.asList((Object[]) parameterMap.get("array"));
			}
		}
		return parameters;
	}

	/**
	 * <p>
	 * 填充主键 ID
	 * </p>
	 * 
	 * @param tableInfo Table Info
	 * @param ms MappedStatement
	 * @param parameterObject
	 *            插入数据库对象
	 * @return
	 */
	protected static Object populateKeys(TableInfo tableInfo, MappedStatement ms, Object parameterObject) {
		if (null != tableInfo && null != tableInfo.getIdType() && tableInfo.getIdType().getKey() >= 2) {
			MetaObject metaObject = ms.getConfiguration().newMetaObject(parameterObject);
			Object idValue = metaObject.getValue(tableInfo.getKeyProperty());
			/* 自定义 ID */
			if (null == idValue || "".equals(idValue)) {
				if (tableInfo.getIdType() == IdType.ID_WORKER) {
					metaObject.setValue(tableInfo.getKeyProperty(), IdWorker.getId());
				} else if (tableInfo.getIdType() == IdType.UUID) {
					metaObject.setValue(tableInfo.getKeyProperty(), get32UUID());
				}
			}
			/* 自定义元对象填充控制器 */
			IMetaObjectHandler metaObjectHandler = MybatisConfiguration.META_OBJECT_HANDLER;
			if (null != metaObjectHandler) {
				metaObjectHandler.insertFill(metaObject);
			}
			return metaObject.getOriginalObject();
		}
		/*
		 * 不处理
		 */
		return parameterObject;
	}

	/**
	 * <p>
	 * 获取去掉"-" UUID
	 * </p>
	 */
	protected static synchronized String get32UUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
