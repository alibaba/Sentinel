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
package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Block exception for request origin access (authority) control.
 *
 * @author youji.zj
 * @author Eric Zhao
 */
public class AuthorityException extends BlockException {

    public AuthorityException(String ruleLimitApp) {
        super(ruleLimitApp);
    }

    public AuthorityException(String ruleLimitApp, AuthorityRule rule) {
        super(ruleLimitApp, rule);
    }

    public AuthorityException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorityException(String ruleLimitApp, String message) {
        super(ruleLimitApp, message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * Get triggered rule.
     * Note: the rule result is a reference to rule map and SHOULD NOT be modified.
     *
     * @return triggered rule
     * @since 1.4.2
     */
    @Override
    public AuthorityRule getRule() {
        return rule.as(AuthorityRule.class);
    }
}
