package com.alibaba.csp.sentinel.adapter.web.common;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

/**
 * Default BlockException Response
 *
 * @author Lingzhi
 */
public enum DefaultBlockExceptionResponse {
    //Too Many Request
    FLOW_EXCEPTION(FlowException.class, 429, "Blocked by Sentinel (flow limiting)"),
    //Too Many Request
    PARAM_FLOW_EXCEPTION(ParamFlowException.class, 429, "Blocked by Sentinel (frequent parameter flow limiting)"),
    //Service Unavailable
    DEGRADE_EXCEPTION(DegradeException.class, 503, "Blocked by Sentinel (circuit breaker)"),
    //Forbidden
    AUTHORITY_EXCEPTION(AuthorityException.class, 403, "Blocked by Sentinel (origin request limiting)"),
    //Too Many Request
    SYSTEM_BLOCK_EXCEPTION(SystemBlockException.class, 429, "Blocked by Sentinel (system limiting)"),
    // Bad Request
    DEFAULT_BLOCK_EXCEPTION(BlockException.class, 400, "Blocked by Sentinel");

    private final Class<? extends BlockException> exp;

    private final int status;
    private final String msg;


    private static final DefaultBlockExceptionResponse[] VALUES;

    static {
        VALUES = values();
    }

    DefaultBlockExceptionResponse(Class<? extends BlockException> exp, int status, String msg) {
        this.exp = exp;
        this.status = status;
        this.msg = msg;
    }


    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public static DefaultBlockExceptionResponse resolve(Class<? extends BlockException> exp) {
        // Use cached VALUES instead of values() to prevent array allocation.
        for (DefaultBlockExceptionResponse res : VALUES) {
            if (res.exp == exp) {
                return res;
            }
        }
        return DEFAULT_BLOCK_EXCEPTION;
    }
}


