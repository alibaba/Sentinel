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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;

/**
 * @author yunfeiyanggzq
 */
public interface ReleaseTokenStrategy {

    /**
     * strategy to store token Id and deal with token keep timeout.
     *
     * @param result       The token result of the request.
     * @param rule         The flow rule this request belong to.
     * @param context      The context of this slot.
     * @param node         The node of this slot.
     * @param acquireCount The count of this request want to occupy.
     * @param prioritized  The priority of this request.
     */
    void doWithResourceTimeoutToken(TokenResult result, FlowRule rule, Context context, DefaultNode node,
                                    int acquireCount, boolean prioritized);

    /**
     * the strategy to exit the flow slot.
     *
     * @param context The context of this slot.
     */
    void releaseTokenWhenExitSlot(Context context);
}
