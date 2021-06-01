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
package com.alibaba.csp.sentinel.slots.system;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author jialiang.linjl
 */
public class SystemBlockException extends BlockException {

    private final String resourceName;

    public SystemBlockException(String resourceName, String message, Throwable cause) {
        super(message, cause);
        this.resourceName = resourceName;
    }

    public SystemBlockException(String resourceName, String limitType) {
        super(limitType);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * Return the limit type of system rule.
     *
     * @return the limit type
     * @since 1.4.2
     */
    public String getLimitType() {
        return getRuleLimitApp();
    }
}
