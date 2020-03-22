package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RedisFlowTestUtil {
    public static void assertResultPass(TokenResult result) {
        assertNotNull(result);
        assertEquals(TokenResultStatus.OK, (int) result.getStatus());
    }

    public static void assertResultBlock(TokenResult result) {
        assertNotNull(result);
        assertEquals(TokenResultStatus.BLOCKED, (int) result.getStatus());
    }

}
