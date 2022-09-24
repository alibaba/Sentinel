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
package com.alibaba.csp.sentinel.cluster.server.codec.netty;

import java.util.List;

import com.alibaba.csp.sentinel.cluster.codec.request.RequestEntityDecoder;
import com.alibaba.csp.sentinel.cluster.request.Request;
import com.alibaba.csp.sentinel.cluster.server.codec.ServerEntityCodecProvider;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class NettyRequestDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        RequestEntityDecoder<ByteBuf, Request> requestDecoder = ServerEntityCodecProvider.getRequestEntityDecoder();
        if (requestDecoder == null) {
            RecordLog.warn("[NettyRequestDecoder] Cannot resolve the global request entity decoder, "
                + "dropping the request");
            return;
        }

        // TODO: handle decode error here.
        Request request = requestDecoder.decode(in);
        if (request != null) {
            out.add(request);
        }
    }
}
