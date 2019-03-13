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
package com.alibaba.csp.sentinel.slots.block.flow;

/**
 * An exception that marks previous prioritized request has been waiting till now, then should pass.
 *
 * @author jialiang.linjl
 * @since 1.5.0
 */
public class PriorityWaitException extends RuntimeException {

    private final long waitInMs;

    public PriorityWaitException(long waitInMs) {
        this.waitInMs = waitInMs;
    }

    public long getWaitInMs() {
        return waitInMs;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
