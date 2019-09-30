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

package com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback;

/**
 * Fall back response for {@link com.alibaba.csp.sentinel.slots.block.BlockException}
 *
 * @author tiger
 */
public class BlockResponse {

    /**
     * HTTP status code.
     */
    private int code;

    private String message;
    private String route;

    public BlockResponse(int code, String message, String route) {
        this.code = code;
        this.message = message;
        this.route = route;
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

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return "{" +
                "\"code\":" + code +
                ", \"message\":" + "\"" + message + "\"" +
                ", \"route\":" + "\"" + route + "\"" +
                '}';
    }
}
