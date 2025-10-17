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
package com.alibaba.csp.sentinel.adapter.jdbc.delegate;

import com.alibaba.csp.sentinel.adapter.jdbc.AbstractSentinelJDBCComponent;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.sql.SQLException;
import java.sql.Wrapper;

/**
 * @author icodening
 * @date 2022.02.08
 */
public class WrapperDelegate<T extends Wrapper> extends AbstractSentinelJDBCComponent implements Wrapper {

    private final T delegate;

    public WrapperDelegate(T delegate) {
        AssertUtil.assertState(delegate != null, "delegate cannot be null");
        this.delegate = delegate;
    }

    @Override
    public <E> E unwrap(Class<E> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public T getDelegate() {
        return delegate;
    }

}
