package com.alibaba.csp.ahas.sentinel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;

/**
 * @author Eric Zhao
 */
public final class AhasSentinelConstants {

    public static final int GATEWAY_TYPE_SCG = 11;
    public static final int GATEWAY_TYPE_ZUUL1 = 12;

    public static final Set<Integer> GATEWAY_APP_TYPES = Collections.unmodifiableSet(
        new HashSet<Integer>() {{
            add(SentinelGatewayConstants.APP_TYPE_GATEWAY);
            add(GATEWAY_TYPE_SCG); add(GATEWAY_TYPE_ZUUL1);
        }}
    );

    private AhasSentinelConstants() {}
}
