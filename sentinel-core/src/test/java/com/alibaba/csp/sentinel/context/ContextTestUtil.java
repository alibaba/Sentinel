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
package com.alibaba.csp.sentinel.context;

import com.alibaba.csp.sentinel.Constants;

/**
 * Util class for testing context-related functions.
 * Only for test. DO NOT USE IN PRODUCTION!
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class ContextTestUtil {

    public static void cleanUpContext() {
        Context context = ContextUtil.getContext();
        if (context != null) {
            context.setCurEntry(null);
            ContextUtil.exit();
        }
    }

    public static void resetContextMap() {
        ContextUtil.resetContextMap();
        Constants.ROOT.removeChildList();
    }

    private ContextTestUtil() {}
}
