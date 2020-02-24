/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.fastjson.JSONObject;

/**
 * @author kaizi2009
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

    public static ResultWrapper error() {

        return new ResultWrapper(-1, "System error");
    }

    public static ResultWrapper blocked() {
        return new ResultWrapper(-2, "Blocked by Sentinel");
    }

    public String toJsonString() {
        return JSONObject.toJSONString(this);
    }
}
