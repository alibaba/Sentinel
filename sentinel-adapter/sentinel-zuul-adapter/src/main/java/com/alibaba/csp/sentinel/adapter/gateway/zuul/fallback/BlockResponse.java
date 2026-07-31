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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback;

/**
 * Fall back response for {@link com.alibaba.csp.sentinel.slots.block.BlockException}
 *
 * @author tiger
 */
public class BlockResponse {

    /**
     * HTTP status code used for the actual HTTP response.
     */
    private int status;

    /**
     * Business code carried in the response body, independent of {@link #status}.
     */
    private int code;

    private String message;
    private String route;

    /**
     * @deprecated use {@link #BlockResponse(int, int, String, String)} instead.
     * The given {@code code} will be used as both the HTTP status and the body code.
     */
    @Deprecated
    public BlockResponse(int code, String message, String route) {
        this(code, code, message, route);
    }

    public BlockResponse(int status, int code, String message, String route) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.route = route;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
