package com.alibaba.csp.sentinel.adapter.pigeon;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.pigeon.fallback.PigeonFallbackRegistry;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.InvokerInterceptor;

public class SentinelPigeonInvokerInterceptor implements InvokerInterceptor {

    @Override
    public void preInvoke(InvokerContext invokerContext) {
        Entry methodEntry = null;
        try {
            String resourceName = PigeonUtils.getResourceName(invokerContext);
            methodEntry = SphU.entry(resourceName, EntryType.OUT);
        } catch (BlockException ex) {
            PigeonFallbackRegistry.getConsumerFallback().handle(invokerContext, ex);
        } catch (RpcException e) {
            Tracer.traceEntry(e, methodEntry);
            throw e;
        } finally {
            if (methodEntry != null) {
                methodEntry.exit();
            }
        }
    }

    @Override
    public void postInvoke(InvokerContext invokerContext) {
    }

    @Override
    public void afterThrowing(InvokerContext invokerContext, Throwable throwable) {
    }

}
