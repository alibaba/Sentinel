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
package com.alibaba.csp.sentinel.cluster.server.codec.data;

import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;
import com.alibaba.csp.sentinel.cluster.response.data.ConcurrentFlowAcquireResponseData;
import io.netty.buffer.ByteBuf;

/**
 * @author yunfeiyanggzq
 */
public class ConcurrentFlowAcquireResponseDataWriter implements EntityWriter<ConcurrentFlowAcquireResponseData, ByteBuf> {
    @Override
    public void writeTo(ConcurrentFlowAcquireResponseData entity, ByteBuf out) {
        if (entity == null || out == null) {
            return;
        }
        out.writeLong(entity.getTokenId());
    }
}