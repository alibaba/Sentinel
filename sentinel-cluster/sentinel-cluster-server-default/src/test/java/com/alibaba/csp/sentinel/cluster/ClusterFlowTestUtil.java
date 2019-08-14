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
package com.alibaba.csp.sentinel.cluster;

import static org.junit.Assert.*;

/**
 * Useful for testing clustered flow control.
 * Only used for test.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterFlowTestUtil {

    public static void assertResultPass(TokenResult result) {
        assertNotNull(result);
        assertEquals(TokenResultStatus.OK, (int) result.getStatus());
    }

    public static void assertResultBlock(TokenResult result) {
        assertNotNull(result);
        assertEquals(TokenResultStatus.BLOCKED, (int) result.getStatus());
    }

    public static void assertResultWait(TokenResult result, int waitInMs) {
        assertNotNull(result);
        assertEquals(TokenResultStatus.SHOULD_WAIT, (int) result.getStatus());
        assertEquals(waitInMs, result.getWaitInMs());
    }

    public static void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ClusterFlowTestUtil() {}
}
