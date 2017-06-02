package com.github.yuxiaobin.mybatis.gm.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Reflection Util.
 * Copy from Mybatis-plus v2.0+.
 * </p>
 *
 * @author yuxiaobin
 */
public class ReflectionUtil {

    /**
     * 获取该类的所有属性列表
     *
     * @param clazz 反射类
     * @return List of Fields
     */
    public static List<Field> getFieldList(Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        List<Field> fieldList = new LinkedList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            /* 过滤静态属性 */
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            /* 过滤 transient关键字修饰的属性 */
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            fieldList.add(field);
        }
        /* 处理父类字段 */
        Class<?> superClass = clazz.getSuperclass();
        if (superClass.equals(Object.class)) {
            return fieldList;
        }
        /* 排除重载属性 */
        return excludeOverrideSuperField(fieldList, getFieldList(superClass));
    }

    /**
     * <p>
     * 排序重置父类属性
     * </p>
     *
     * @param fieldList      子类属性
     * @param superFieldList 父类属性
     * @return List of Fields
     */
    public static List<Field> excludeOverrideSuperField(List<Field> fieldList, List<Field> superFieldList) {
        // 子类属性
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fieldList) {
            fieldMap.put(field.getName(), field);
        }
        for (Field superField : superFieldList) {
            if (null == fieldMap.get(superField.getName())) {
                // 加入重置父类属性
                fieldList.add(superField);
            }
        }
        return fieldList;
    }
}
