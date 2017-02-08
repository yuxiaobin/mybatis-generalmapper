/**
 * Copyright (c) 2016-2017, Kelly Lake (179634696@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.yuxiaobin.mybatis.gm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Import;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.SqlMethod;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.github.yuxiaobin.mybatis.gm.conf.GeneralMapperBootstrapConfiguration;
import com.github.yuxiaobin.mybatis.gm.mapper.GeneralMapperSqlInjector.ExtraSqlMethod;
import com.github.yuxiaobin.mybatis.gm.processer.MybatisGeneralEntityProcessor;

/**
 * @author Kelly Lake(179634696@qq.com)
 */
@Import(GeneralMapperBootstrapConfiguration.class)
public class GeneralMapper {

	private SqlSessionTemplate sqlSessionTemplate;

	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		try {
			this.sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public GeneralMapper() {
		super();
	}

	public GeneralMapper(SqlSessionFactory sqlSessionFactory) {
		super();
		this.sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
	}

	/**
	 * 插入一条记录 Insert one record. PK will be auto filled by the
	 * strategy(UUID/ID_WORKER auto filled by framework) {@code @TableId}
	 * {@code IdType}:
	 * <ul>
	 * <li>AUTO: DB auto_increment</li>
	 * <li>INPUT: user input(will not auto fill)</li>
	 * <li>UUID: uuid varchar(32), auto filled if is null</li>
	 * <li>ID_WORKER: int(64), auto filled if is null, sample generated ID:
	 * 810683965887864834</li>
	 * </ul>
	 *
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	public int insert(Object entity) {
		return sqlSessionTemplate.insert(getSqlStatement(SqlMethod.INSERT_ONE.getMethod(), entity.getClass()), entity);
	}

	/**
	 * <p>
	 * 插入一条记录（选择字段， null 字段不插入） Insert one record, null column will be
	 * ignored(if DB column has default value, then value of the DB record will
	 * be the default value)
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	public int insertSelective(Object entity) {
		return sqlSessionTemplate.insert(getSqlStatement(SqlMethod.INSERT_ONE_SELECTIVE.getMethod(), entity.getClass()),
				entity);
	}

	/**
	 * <p>
	 * 插入（批量） Insert batch(current version only support mysql/oracle)
	 * </p>
	 *
	 * @param entityList
	 *            实体对象列表
	 * @return int
	 */
	public int insertBatch(List<?> entityList) {
		return sqlSessionTemplate.insert(
				getSqlStatement(SqlMethod.INSERT_BATCH_MYSQL.getMethod(), entityList.get(0).getClass()), entityList);
	}

	/**
	 * <p>
	 * 根据 ID 删除
	 * </p>
	 *
	 * @param id
	 *            主键ID
	 * @param clazz
	 *            对象类型
	 * @return int
	 */
	public int deleteById(Object id, Class<?> clazz) {
		return sqlSessionTemplate.delete(getSqlStatement(SqlMethod.DELETE_BY_ID.getMethod(), clazz), id);
	}
	/**
	 * <p>
	 * 根据EntityWrapper条件删除记录
	 * </p>
	 *
	 * @param entityWrapper
	 *            条件封装(必须setEntity)
	 * @return int
	 * 
	 * @since 1.4
	 */
	public int deleteByEW(EntityWrapper<?> entityWrapper) {
		return sqlSessionTemplate.delete(getSqlStatement(ExtraSqlMethod.DELETE_BY_EW.getMethod(), entityWrapper.getEntity().getClass()),
				asParam("ew", entityWrapper));
	}

	/**
	 * <p>
	 * 根据 columnMap 条件，删除记录
	 * </p>
	 *
	 * @param columnMap
	 *            表字段 map 对象
	 * @param clazz
	 *            对象类型
	 * @return int
	 */
	public int deleteByMap(Map<String, Object> columnMap, Class<?> clazz) {
		return sqlSessionTemplate.delete(getSqlStatement(SqlMethod.DELETE_BY_MAP.getMethod(), clazz),
				asParam("cm", columnMap));
	}

	/**
	 * <p>
	 * 根据 entity 条件，删除记录
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	public int deleteSelective(Object entity) {
		return sqlSessionTemplate.delete(getSqlStatement(SqlMethod.DELETE_SELECTIVE.getMethod(), entity.getClass()),
				asParam("ew", entity));
	}

	/**
	 * <p>
	 * 删除（根据ID 批量删除）
	 * </p>
	 *
	 * @param idList
	 *            主键ID列表
	 * @param clazz
	 *            对象类型
	 * @return int
	 */
	public int deleteBatchIds(List<?> idList, Class<?> clazz) {
		return sqlSessionTemplate.delete(getSqlStatement(SqlMethod.DELETE_BATCH.getMethod(), clazz), idList);
	}

	/**
	 * <p>
	 * 根据 ID 修改
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	public int updateById(Object entity) {
		return sqlSessionTemplate.update(getSqlStatement(SqlMethod.UPDATE_BY_ID.getMethod(), entity.getClass()),
				asParam("et", entity));
	}

	/**
	 * <p>
	 * 根据 ID 选择修改
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	public int updateSelectiveById(Object entity) {
		return sqlSessionTemplate.update(
				getSqlStatement(SqlMethod.UPDATE_SELECTIVE_BY_ID.getMethod(), entity.getClass()),
				asParam("et", entity));
	}

	/**
	 * <p>
	 * 根据 whereEntity 条件，更新记录
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @param whereEntity
	 *            实体查询条件（可以为 null）
	 * @return int
	 */
	public int update(Object entity, Object whereEntity) {
		Map<String, Object> objectMap = asParam("et", entity);
		objectMap.putAll(asParam("ew", whereEntity));
		return sqlSessionTemplate.update(getSqlStatement(SqlMethod.UPDATE.getMethod(), entity.getClass()), objectMap);
	}

	/**
	 * <p>
	 * 根据 whereEntity 条件，选择更新记录 Update record(s) by where entity(by property
	 * values which is not null)
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @param whereEntity（可以为
	 *            null） 实体查询条件
	 * @return int
	 */
	public int updateSelective(Object entity, Object whereEntity) {
		Map<String, Object> objectMap = asParam("et", entity);
		objectMap.putAll(asParam("ew", whereEntity));
		return sqlSessionTemplate.update(getSqlStatement(SqlMethod.UPDATE_SELECTIVE.getMethod(), entity.getClass()),
				objectMap);
	}

	/**
	 * <p>
	 * 根据ID 批量更新
	 * </p>
	 *
	 * @param entityList
	 *            实体对象列表
	 * @return int
	 */
	public int updateBatchById(List<?> entityList) {
		return sqlSessionTemplate.update(
				getSqlStatement(SqlMethod.UPDATE_BATCH_BY_ID_MYSQL.getMethod(), entityList.get(0).getClass()),
				entityList);
	}

	/**
	 * <p>
	 * 根据 ID 查询
	 * </p>
	 *
	 * @param id
	 *            主键ID
	 * @param clazz
	 *            对象类型
	 * @return T
	 */
	public <T> T selectById(Object id, Class<T> clazz) {
		return sqlSessionTemplate.selectOne(getSqlStatement(SqlMethod.SELECT_BY_ID.getMethod(), clazz), id);
	}

	;

	/**
	 * <p>
	 * 查询（根据ID 批量查询）
	 * </p>
	 *
	 * @param idList
	 *            主键ID列表
	 * @param clazz
	 *            对象类型
	 * @return List
	 */
	public <T> List<T> selectBatchIds(List<?> idList, Class<T> clazz) {
		return sqlSessionTemplate.selectList(getSqlStatement(SqlMethod.SELECT_BATCH.getMethod(), clazz), idList);
	}

	/**
	 * <p>
	 * 查询（根据 columnMap 条件）
	 * </p>
	 *
	 * @param columnMap
	 *            表字段 map 对象
	 * @param clazz
	 *            表字段 map 对象类型
	 * @return List
	 */
	public <T> List<T> selectByMap(Map<String, Object> columnMap, Class<T> clazz) {
		return sqlSessionTemplate.selectList(getSqlStatement(SqlMethod.SELECT_BY_MAP.getMethod(), clazz),
				asParam("cm", columnMap));
	}

	/**
	 * <p>
	 * 根据 entity 条件，查询一条记录
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @return object
	 */
	public <T> T selectOne(T entity) {
		return sqlSessionTemplate.selectOne(getSqlStatement(SqlMethod.SELECT_ONE.getMethod(), entity.getClass()),
				asParam("ew", entity));
	}

	/**
	 * <p>
	 * 根据 entity 条件，查询总记录数
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @return int
	 */
	public int selectCount(Object entity) {
		return sqlSessionTemplate.selectOne(getSqlStatement(SqlMethod.SELECT_COUNT.getMethod(), entity.getClass()),
				asParam("ew", entity));
	}

	/**
	 * <p>
	 * Select count by entityWrapper.
	 * </p>
	 * 
	 * @param entityWrapper
	 * @return
	 */
	public <T> int selectCountByEW(EntityWrapper<T> entityWrapper) {
		return sqlSessionTemplate.selectOne(
				getSqlStatement(SqlMethod.SELECT_COUNT_EW.getMethod(), entityWrapper.getEntity().getClass()),
				asParam("ew", entityWrapper));
	}

	/**
	 * <p>
	 * 根据 entity 条件，查询全部记录
	 * </p>
	 *
	 * @param entityWrapper
	 *            实体对象封装操作类（可以为 null）
	 * @return List
	 */
	public <T> List<T> selectList(EntityWrapper<T> entityWrapper) {
		return sqlSessionTemplate.selectList(
				getSqlStatement(SqlMethod.SELECT_LIST.getMethod(), entityWrapper.getEntity().getClass()),
				asParam("ew", entityWrapper));
	}

	/**
	 * <p>
	 * 根据 entity 条件，查询全部记录（并翻页）
	 * </p>
	 *
	 * @param page
	 *            分页查询条件（可以为 RowBounds.DEFAULT）
	 * @param entityWrapper
	 *            实体对象封装操作类（可以为 null）
	 * @return List
	 */
	public <T> List<T> selectPage(Pagination page, EntityWrapper<T> entityWrapper) {
		return sqlSessionTemplate.selectList(
				getSqlStatement(SqlMethod.SELECT_PAGE.getMethod(), entityWrapper.getEntity().getClass()),
				asParam("ew", entityWrapper), page);
	}

	@SuppressWarnings("serial")
	public Map<String, Object> asParam(final String paramName, final Object obj) {
		return new HashMap<String, Object>() {
			{
				put(paramName, obj);
			}
		};
	}

	private String getSqlStatement(String statement, Class<?> clazz) {
		return MybatisGeneralEntityProcessor.generateNamespace(getCorrespondingEntityClass(clazz)) + "." + statement;
	}

	/**
	 * 开放接口供子类实现特殊逻辑
	 * （比如entityWrapper里面的实体其实是VO extends Entity.
	 * 		复写该方法实现获取Entity.class) 
	 * This method to allow subtypes to implement
	 * special logic: like use entityVO extends Entity for query _ get correct Entity class 
	 * 
	 * 获取子类，可以通过(How to get the correct entity class?) Use
	 * org.reflections.Reflections： 确保entity 和VO都在这个package下(make sure the package includes entity and entityVO);
	 * Reflections reflections = new Reflections("com.xx"); - 获取所有子类的class; 
	 * Set&lt;?&gt; subClazzs = reflections.getSubTypesOf(entityClazz);
	 * 
	 * @author 179634696@qq.com
	 * @param clazz
	 * @return
	 */
	protected Class<?> getCorrespondingEntityClass(Class<?> clazz){
		return clazz;
	}

}
