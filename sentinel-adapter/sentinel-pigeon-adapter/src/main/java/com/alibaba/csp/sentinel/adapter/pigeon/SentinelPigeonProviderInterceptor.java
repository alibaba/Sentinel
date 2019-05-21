package com.alibaba.csp.sentinel.adapter.pigeon;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.pigeon.fallback.PigeonFallbackRegistry;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderInterceptor;

import java.lang.reflect.Parameter;

public class SentinelPigeonProviderInterceptor implements ProviderInterceptor {

    @Override
    public void preInvoke(ProviderContext providerContext) {
        Entry methodEntry = null;
        Parameter[] arguments = new Parameter[]{};
        try {
            String resourceName = PigeonUtils.getResourceName(providerContext);
            arguments = PigeonUtils.getMethodArguments(providerContext);
            ContextUtil.enter(resourceName);
            methodEntry = SphU.entry(resourceName, EntryType.IN, 1, arguments);
        } catch (BlockException ex) {
            PigeonFallbackRegistry.getProviderFallback().handle(providerContext, ex);
        } catch (RpcException ex) {
            Tracer.traceEntry(ex, methodEntry);
            throw ex;
        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1, arguments);
            }
            ContextUtil.exit();
        }
    }

    @Override
    public void postInvoke(ProviderContext providerContext) {
    }

}
