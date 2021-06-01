/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport.command.http;

/**
 * @author Jason Joo
 */
public enum StatusCode {
    /**
     * 200 OK.
     */
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    LENGTH_REQUIRED(411, "Length Required"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    
    private int code;
    private String desc;
    private String representation;
    
    StatusCode(int code, String desc) {
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
