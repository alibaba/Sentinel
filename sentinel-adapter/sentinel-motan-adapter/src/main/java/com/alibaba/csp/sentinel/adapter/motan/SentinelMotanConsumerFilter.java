package com.alibaba.csp.sentinel.adapter.motan;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.motan.config.MotanAdapterGlobalConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.exception.MotanAbstractException;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;

/**
 * program: sentinel-parent
 * description: ${description}
 * author: zxn
 * create: 2020-10-28 00:41
 **/
@Activation(key =MotanConstants.NODE_TYPE_REFERER)
@SpiMeta(name = "sentinelMotanConsumer")
public class SentinelMotanConsumerFilter extends BaseMotanSentinelFilter {

    public SentinelMotanConsumerFilter(){
        RecordLog.info("Sentinel motan consumer filter initialized");
    }

    @Override
    String getMethodName(Caller<?> caller, Request request, String prefix) {
        return MotanUtils.getMethodResourceName(caller, request, prefix);
    }

    @Override
    String getInterfaceName(Caller<?> caller, String prefix) {
        return MotanUtils.getInterfaceName(caller, prefix);
    }

    @Override
    public Response filter(Caller<?> caller, Request request) {
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        String prefix = MotanAdapterGlobalConfig.getMotanConsumerPrefix();
        String interfaceResourceName = getInterfaceName(caller, prefix);
        String methodResourceName = getMethodName(caller, request, prefix);
        try {
            interfaceEntry = SphU.entry(interfaceResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT);
            methodEntry = SphU.entry(methodResourceName, ResourceTypeConstants.COMMON_RPC, EntryType.OUT,
                    request.getArguments());
            Response result = caller.call(request);
            if (result.getException() != null) {
                Tracer.traceEntry(result.getException(), interfaceEntry);
                Tracer.traceEntry(result.getException(), methodEntry);
            }
            return result;
        } catch (BlockException e) {
            return MotanAdapterGlobalConfig.getConsumerFallback().handle(caller, request, e);
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
        }
    }
}
