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
package com.alibaba.csp.sentinel.adapter.dubbo3;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.filter.ClusterFilter;
import org.apache.dubbo.rpc.model.ApplicationModel;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * Puts current consumer's application name in the attachment of each invocation.
 *
 * @author Eric Zhao
 */
@Activate(group = CONSUMER)
public class DubboAppContextFilter implements ClusterFilter {

    private final String applicationName;

    public DubboAppContextFilter(ApplicationModel applicationModel) {
        this.applicationName = applicationModel.tryGetApplicationName();
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (applicationName != null && !applicationName.isEmpty()) {
            invocation.setAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, applicationName);
        }
        return invoker.invoke(invocation);
    }
}
