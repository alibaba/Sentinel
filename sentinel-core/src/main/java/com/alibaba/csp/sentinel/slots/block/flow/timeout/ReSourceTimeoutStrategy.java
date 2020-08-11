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
package com.alibaba.csp.sentinel.slots.block.flow.timeout;

import com.alibaba.csp.sentinel.context.Context;
import io.netty.util.Timeout;

/**
 * @author yunfeiyanggzq
 */
public interface ReSourceTimeoutStrategy {

    /**
     * strategy to deal with resource called timeout.
     *
     * @param tokenId The Id of token.
     */
    void doWithSourceTimeout( long tokenId);

}
