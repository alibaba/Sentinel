/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.common.slot;

import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;

/**
 * @author Eric Zhao
 * @since 1.6.1
 *
 * @deprecated since 1.7.2, we can use @SpiOrder(-4000) to adjust the order of {@link GatewayFlowSlot},
 * this class is reserved for compatibility with older versions.
 * @see GatewayFlowSlot
 * @see DefaultSlotChainBuilder
 */
@Deprecated
public class GatewaySlotChainBuilder extends DefaultSlotChainBuilder {

}
