package com.taobao.diamond.domain;

import java.io.Serializable;

/**
 * rest result class
 * 
 * @author diamond
 *
 * @param <T>
 */

public class RestResult<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6095433538316185017L;

	private int code;
	private String message;
	private T data;

	public RestResult() {
	}

	public RestResult(int code, String message, T data) {
		this.code = code;
		this.setMessage(message);
		this.data = data;
	}

	public RestResult(int code, T data) {
		this.code = code;
		this.data = data;
	}

	public RestResult(int code, String message) {
		this.code = code;
		this.setMessage(message);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}


}
