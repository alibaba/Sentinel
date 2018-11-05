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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Block exception for frequent ("hot-spot") parameter flow control.
 *
 * @author jialiang.linjl
 * @since 0.2.0
 */
public class ParamFlowException extends BlockException {

    private final String resourceName;

    public ParamFlowException(String resourceName, String message, Throwable cause) {
        super(message, cause);
        this.resourceName = resourceName;
    }

    public ParamFlowException(String resourceName, String message) {
        super(message, message);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
