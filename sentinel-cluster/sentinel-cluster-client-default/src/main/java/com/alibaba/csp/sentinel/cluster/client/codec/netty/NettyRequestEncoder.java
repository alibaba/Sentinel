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
package com.alibaba.csp.sentinel.cluster.client.codec.netty;

import com.alibaba.csp.sentinel.cluster.client.codec.ClientEntityCodecProvider;
import com.alibaba.csp.sentinel.cluster.codec.request.RequestEntityWriter;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.Request;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class NettyRequestEncoder extends MessageToByteEncoder<ClusterRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ClusterRequest request, ByteBuf out) throws Exception {
        RequestEntityWriter<Request, ByteBuf> requestEntityWriter = ClientEntityCodecProvider.getRequestEntityWriter();
        if (requestEntityWriter == null) {
            RecordLog.warn("[NettyRequestEncoder] Cannot resolve the global request entity writer, dropping the request");
            return;
        }

        requestEntityWriter.writeTo(request, out);
    }
}
