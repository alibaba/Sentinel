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
package com.alibaba.csp.sentinel.cluster.client.codec;

import com.alibaba.csp.sentinel.cluster.client.codec.registry.RequestDataWriterRegistry;
import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;
import com.alibaba.csp.sentinel.cluster.codec.request.RequestEntityWriter;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.Request;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.buffer.ByteBuf;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class DefaultRequestEntityWriter implements RequestEntityWriter<ClusterRequest, ByteBuf> {

    @Override
    public void writeTo(ClusterRequest request, ByteBuf target) {
        int type = request.getType();
        EntityWriter<Object, ByteBuf> requestDataWriter = RequestDataWriterRegistry.getWriter(type);

        if (requestDataWriter == null) {
            RecordLog.warn("[DefaultRequestEntityWriter] Cannot find matching request writer for type <{}>,"
                + " dropping the request", type);
            return;
        }
        // Write head part of request.
        writeHead(request, target);
        // Write data part.
        requestDataWriter.writeTo(request.getData(), target);
    }

    private void writeHead(Request request, ByteBuf out) {
        out.writeInt(request.getId());
        out.writeByte(request.getType());
    }
}
