package com.github.yuxiaobin.mybatis.gm.utils;

import java.lang.reflect.Proxy;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public abstract class MybatisPluginUtil {

	/**
	 * 获得真正的处理对象,可能多层代理.<BR>
	 * Get the real object for mybatis plugin.
	 */
	public static Object getRealTarget(Object target) {
		if (Proxy.isProxyClass(target.getClass())) {
			MetaObject mo = SystemMetaObject.forObject(target);
			return getRealTarget(mo.getValue("h.target"));
		}
		return target;
	}
}
