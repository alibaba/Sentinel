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
package com.alibaba.csp.sentinel.adapter.dubbo;


import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

/**
 * Base Class of the {@link SentinelDubboProviderFilter} and {@link SentinelDubboConsumerFilter}.
 *
 * @author Zechao Zheng
 */

public abstract class BaseSentinelDubboFilter implements Filter {


    /**
     * Get method name of dubbo rpc
     *
     * @param invoker
     * @param invocation
     * @return
     */
    abstract String getMethodName(Invoker invoker, Invocation invocation);

    /**
     * Get interface name of dubbo rpc
     *
     * @param invoker
     * @return
     */
    abstract String getInterfaceName(Invoker invoker);


}
