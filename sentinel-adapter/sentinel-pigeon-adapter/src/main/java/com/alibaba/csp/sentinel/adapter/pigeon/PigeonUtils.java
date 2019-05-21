package com.alibaba.csp.sentinel.adapter.pigeon;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

import java.lang.reflect.Parameter;

public final class PigeonUtils {

    public static String getResourceName(InvocationContext context) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(context.getRequest().getServiceName())
                .append(":")
                .append(context.getRequest().getMethodName())
                .append("(");
        boolean isFirst = true;
        for (String clazz : context.getRequest().getParamClassName()) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz);
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

    public static Parameter[] getMethodArguments(ProviderContext providerContext) {
        return providerContext.getServiceMethod().getMethod().getParameters();
    }

    private PigeonUtils() {}

}
