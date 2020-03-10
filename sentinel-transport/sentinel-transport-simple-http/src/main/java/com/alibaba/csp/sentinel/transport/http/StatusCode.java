package com.alibaba.csp.sentinel.transport.http;

public enum StatusCode {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    LENGTH_REQUIRED(411, "Length Required"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    
    private int code;
    private String desc;
    private String representation;
    
    private StatusCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
        this.representation = code + " " + desc;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    @Override
    public String toString() {
        return representation;
    }
}
