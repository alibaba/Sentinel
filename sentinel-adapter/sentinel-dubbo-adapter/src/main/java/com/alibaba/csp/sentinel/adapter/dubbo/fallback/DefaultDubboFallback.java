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
package com.alibaba.csp.sentinel.adapter.dubbo.fallback;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.fallback.FallbackRule;
import com.alibaba.csp.sentinel.fallback.FallbackRuleManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.ParseException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;

/**
 * @author Eric Zhao
 */
public class DefaultDubboFallback implements DubboFallback {

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        return defaultFallback(ex);
    }

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex, String resourceName) {
        FallbackRule fallbackRule = FallbackRuleManager.getFallbackRule(resourceName);
        if (StringUtil.isNotEmpty(fallbackRule.getFallback())) {
            // return null
            if (Constants.NULL_FALLBACK.equals(fallbackRule.getFallback())) {
                return null;
            // throw exception
            } else if (Constants.EXCEPTION_FALLBACK.equals(fallbackRule.getFallback())) {
                throw new SentinelRpcException(ex.toRuntimeException());
            // return fallback object
            } else {
                if (StringUtil.isNotEmpty(fallbackRule.getClazzReference())) {
                        RpcResult result = new RpcResult();
                        try {
                            result.setValue(JSON.parse(fallbackRule.getFallback(), fallbackRule.getClass()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            result.setException(new SentinelRpcException(ex.toRuntimeException()));
                        }
                        return result;
                } else {
                    return defaultFallback(ex);
                }
            }
        } else {
            return defaultFallback(ex);
        }
    }

    private Result defaultFallback(BlockException ex) {
        // Just wrap the exception. edit by wzg923 2020/9/23
        RpcResult result = new RpcResult();
        result.setException(new SentinelRpcException(ex.toRuntimeException()));
        return result;
    }
}
