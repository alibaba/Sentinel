package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangkai
 */
public class ResultWrapper {

    private Integer code;
    private String message;

    public ResultWrapper(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static ResultWrapper blocked() {
        return new ResultWrapper(-1, "Blocked by Sentinel");
    }

    public String toJsonString() {
        return JSONObject.toJSONString(this);
    }
}
