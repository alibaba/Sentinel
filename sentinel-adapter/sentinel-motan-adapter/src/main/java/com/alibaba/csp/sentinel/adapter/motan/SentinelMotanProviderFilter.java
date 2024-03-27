/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.motan;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.motan.config.MotanAdapterGlobalConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.exception.MotanAbstractException;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import java.util.Map;

/**
 * @author zhangxn8
 */
@Activation(key = MotanConstants.NODE_TYPE_SERVICE)
@SpiMeta(name = MotanAdapterGlobalConfig.SENTINEL_MOTAN_PROVIDER)
public class SentinelMotanProviderFilter implements Filter {

    public SentinelMotanProviderFilter(){
        RecordLog.info("Sentinel motan provider filter initialized");
    }

    @Override
    public Response filter(Caller<?> caller, Request request) {
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        Map<String, String> attachment = request.getAttachments();
        String origin = attachment.getOrDefault(MotanAdapterGlobalConfig.APPLICATION, MotanAdapterGlobalConfig.MOTAN);
        String prefix = MotanAdapterGlobalConfig.getMotanProviderPrefix();
        String interfaceResourceName = MotanUtils.getInterfaceName(caller, prefix);
        String methodResourceName = MotanUtils.getMethodResourceName(caller, request, prefix);
        try {
            ContextUtil.enter(methodResourceName, origin);
            interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN);
            methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.IN,
                    request.getArguments());
            Response result = caller.call(request);
            if (result.getException() != null) {
                Tracer.traceEntry(result.getException(), interfaceEntry);
                Tracer.traceEntry(result.getException(), methodEntry);
            }
            return result;
        } catch (BlockException e) {
            return MotanAdapterGlobalConfig.getProviderFallback().handle(caller, request, e);
        } catch (MotanAbstractException e) {
            Tracer.traceEntry(e, interfaceEntry);
            Tracer.traceEntry(e, methodEntry);
            throw e;
        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1, request.getArguments());
            }
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
            ContextUtil.exit();
        }
    }
}
