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
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * Note: Only for test. DO NOT USE IN PRODUCTION!
 *
 * @author cdfive
 */
public class CtEntryTestUtil {

    /**
     * This method is used to create CtEntry Object.
     */
    public static CtEntry buildCtEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context) {
        return new CtEntry(resourceWrapper, chain, context);
    }
}
