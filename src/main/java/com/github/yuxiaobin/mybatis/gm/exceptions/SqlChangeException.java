package com.github.yuxiaobin.mybatis.gm.exceptions;

public class SqlChangeException extends Exception {

	private static final long serialVersionUID = 1L;

	public SqlChangeException() {
		super();
	}

	public SqlChangeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SqlChangeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlChangeException(String message) {
		super(message);
	}

	public SqlChangeException(Throwable cause) {
		super(cause);
	}

	
}
