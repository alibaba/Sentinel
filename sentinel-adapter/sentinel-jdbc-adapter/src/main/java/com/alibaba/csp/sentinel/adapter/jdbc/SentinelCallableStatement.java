/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.jdbc;

import com.alibaba.csp.sentinel.adapter.jdbc.delegate.CallableStatementDelegate;

import java.sql.CallableStatement;
import java.util.function.Function;

/**
 * @author icodening
 * @date 2022.02.09
 */
public class SentinelCallableStatement extends CallableStatementDelegate {

    public SentinelCallableStatement(CallableStatement delegate) {
        super(delegate);
    }

    public SentinelCallableStatement(CallableStatement delegate, Function<String, String> sqlMapper) {
        super(delegate);
        setSQLMapper(sqlMapper);
    }
}
