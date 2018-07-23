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
package com.alibaba.csp.sentinel.slots.block;

/**
 * A {@link RuntimeException} marks sentinel RPC exception. The stack trace
 * is removed for high performance.
 *
 * @author leyou
 */
public class SentinelRpcException extends RuntimeException {

    public SentinelRpcException(String msg) {
        super(msg);
    }

    public SentinelRpcException(Throwable e) {
        super(e);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
