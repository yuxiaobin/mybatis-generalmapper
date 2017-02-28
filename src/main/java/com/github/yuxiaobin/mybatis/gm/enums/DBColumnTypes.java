package com.github.yuxiaobin.mybatis.gm.enums;

/**
 * 数据库类型跟java类型的枚举
 * 
 * @author yuxiaobin
 *
 */
public enum DBColumnTypes {

	STRING("String", null),
	LONG("Long", null),
	INTEGER("Integer", null),
	FLOAT("Float", null),
	DOUBLE("Double", null),
	BOOLEAN("Boolean", null),
	BYTE_ARRAY("byte[]", null),
	CHARACTER("Character", null),
	OBJECT("Object", null),
	DATE("Date", "java.util.Date"),
	TIME("Time", "java.sql.Time"),
	BLOB("Blob", "java.sql.Blob"),
	CLOB("Clob", "java.sql.Clob"),
	TIMESTAMP("Timestamp", "java.sql.Timestamp"),
	BIG_INTEGER("BigInteger", "java.math.BigInteger"),
	BIG_DECIMAL("BigDecimal", "java.math.BigDecimal");

	/** 类型 */
	private final String type;

	/** 包路径 */
	private final String pkg;
	

	DBColumnTypes(final String type, final String pkg) {
		this.type = type;
		this.pkg = pkg;
	}

	public String getType() {
		return this.type;
	}

	public String getPkg() {
		return this.pkg;
	}

}
