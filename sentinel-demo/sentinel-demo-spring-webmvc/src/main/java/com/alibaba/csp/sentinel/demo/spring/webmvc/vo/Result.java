package com.alibaba.csp.sentinel.demo.spring.webmvc.vo;

import com.alibaba.fastjson.JSONObject;

/**
 * @author taodizhou
 * @description
 * @date 2019-09-25
 */
public class Result<T> {
  public static final int SUCCESS_CODE = 0;
  public static final String SUCCESS_MSG = "Successful";
  public static final int ERROR_CODE = -1;
  public static final String ERROR_MSG = "Failed";
  public static final int BLOCKED_CODE = 10;
  public static final String BLOCKED_MSG = "Blocked by Sentinel (flow limiting)";
  private int code;
  private String msg;
  private T data;

  public Result(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public static Result blocked() {
    return new Result(BLOCKED_CODE, BLOCKED_MSG);
  }

  public static Result success() {
    return new Result(SUCCESS_CODE, SUCCESS_MSG);
  }

  public static Result error() {
    return new Result(ERROR_CODE, ERROR_MSG);
  }

  public String toJsonString() {
    return JSONObject.toJSONString(this);
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
