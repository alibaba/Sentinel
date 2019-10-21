package com.taobao.diamond.exception;

/**
 * Diamond exception
 * 
 * @author Diamond
 *
 */
public class DiamondException extends Exception {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3913902031489277776L;

	private int errCode;

	private String errMsg;
	
	public DiamondException() {

	}

	public DiamondException(Throwable cause) {
		super(cause);
	}

	public DiamondException(String errMsg, Throwable cause) {
		super(errMsg, cause);
		this.errMsg = errMsg;
	}

	public DiamondException(int errCode, String errMsg) {
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public DiamondException(int errCode, String errMsg, Throwable cause) {
		super(errMsg, cause);
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public int getErrCode() {
		return errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	@Override
	public String toString() {
		return "ErrCode:" + errCode + ",ErrMsg:" + errMsg;
	}

	/**
	 * client error code 
	 * -400 -503 throw exception to user
	 */
	// invalid param（参数错误）
	public static final int CLIENT_INVALID_PARAM = -400;
	// over client threshold（超过server端的限流阈值）
	public static final int CLIENT_OVER_THRESHOLD = -503;

	/**
	 * server error code 
	 * 400 403 throw exception to user
	 * 500 502 503 change ip and retry
	 */
	// invalid param（参数错误）
	public static final int INVALID_PARAM = 400;
	// no right（鉴权失败）
	public static final int NO_RIGHT = 403;
	// conflict（写并发冲突）
	public static final int CONFLICT = 409;
	// server error（server异常，如超时）
	public static final int SERVER_ERROR = 500;
	// bad gateway（路由异常，如nginx后面的DiamondServer挂掉）
	public static final int BAD_GATEWAY = 502;
	// over threshold（超过server端的限流阈值）
	public static final int OVER_THRESHOLD = 503;
	
}
