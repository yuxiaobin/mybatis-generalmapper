package com.github.yuxiaobin.mybatis.gm.utils;

import org.springframework.util.StringUtils;

public class GeneralJdbcReflectionUtil {

	public static enum DBType {
		/**
		 * MYSQL
		 */
		MYSQL("mysql"),
		/**
		 * ORACLE
		 */
		ORACLE("oracle"),
		/**
		 * DB2
		 */
		DB2("db2"),
		/**
		 * H2
		 */
		H2("h2"),
		/**
		 * HSQL
		 */
//		HSQL("hsql", "", "HSQL数据库"),
		/**
		 * SQLITE
		 */
		SQLITE("sqlite"),
		/**
		 * POSTGRE
		 */
		POSTGRE("postgresql"),
		/**
		 * SQLSERVER2005
		 */
		SQLSERVER2005("sqlserver2005"),
		/**
		 * SQLSERVER
		 */
		SQLSERVER("sqlserver"),
		/**
		 * UNKONWN DB
		 */
		OTHER("other");

		private final String db;

		DBType(final String db) {
			this.db = db;
		}

		/**
		 * <p>
		 * 获取数据库类型（默认 MySql）
		 * </p>
		 *
		 * @param dbType
		 *            数据库类型字符串
		 * @return
		 */
		public static DBType getDBType(String dbType) {
			DBType[] dts = DBType.values();
			for (DBType dt : dts) {
				if (dt.getDb().equalsIgnoreCase(dbType)) {
					return dt;
				}
			}
			return MYSQL;
		}

		public String getDb() {
			return this.db;
		}

	}
	
	/**
	 * <p>
	 * 根据连接地址判断数据库类型
	 * </p>
	 * 
	 * @param jdbcUrl
	 *            连接地址
	 * @return
	 */
	public static DBType getDbType(String jdbcUrl) {
		if (StringUtils.isEmpty(jdbcUrl)) {
			return DBType.MYSQL;
		}
		if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:cobar:")
				|| jdbcUrl.startsWith("jdbc:log4jdbc:mysql:")) {
			return DBType.MYSQL;
		} else if (jdbcUrl.startsWith("jdbc:oracle:") || jdbcUrl.startsWith("jdbc:log4jdbc:oracle:")) {
			return DBType.ORACLE;
		} else if (jdbcUrl.startsWith("jdbc:microsoft:") || jdbcUrl.startsWith("jdbc:log4jdbc:microsoft:")) {
			return DBType.SQLSERVER;
		} else if (jdbcUrl.startsWith("jdbc:sqlserver:") || jdbcUrl.startsWith("jdbc:log4jdbc:sqlserver:")) {
			return DBType.SQLSERVER;
		} else if (jdbcUrl.startsWith("jdbc:postgresql:") || jdbcUrl.startsWith("jdbc:log4jdbc:postgresql:")) {
			return DBType.POSTGRE;
		} else if (jdbcUrl.startsWith("jdbc:hsqldb:") || jdbcUrl.startsWith("jdbc:log4jdbc:hsqldb:")) {
			return null;
		} else if (jdbcUrl.startsWith("jdbc:db2:")) {
			return DBType.DB2;
		} else if (jdbcUrl.startsWith("jdbc:sqlite:")) {
			return DBType.SQLITE;
		} else if (jdbcUrl.startsWith("jdbc:h2:") || jdbcUrl.startsWith("jdbc:log4jdbc:h2:")) {
			return DBType.H2;
		} else {
			return DBType.OTHER;
		}
	}

	
}
