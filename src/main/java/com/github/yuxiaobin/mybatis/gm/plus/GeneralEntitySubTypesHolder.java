package com.github.yuxiaobin.mybatis.gm.plus;

import java.util.HashMap;
import java.util.Map;

/**
 *	用来存实体和对应子类的匹配关系
 * 	Map.key=子类的class
 * 	Map.value=真正实体类的class
 * 
 * @author yuxiaobin
 *
 */
public class GeneralEntitySubTypesHolder {
	
	private static final Map<Class<?>,Class<?>> SUB_TYPES = new HashMap<>();
	
	public static void put(Class<?> subClazz, Class<?> entityClazz){
		SUB_TYPES.put(subClazz, entityClazz);
	}
	
	public static Class<?> get(Class<?> subClazz){
		return SUB_TYPES.get(subClazz);
	}

}
