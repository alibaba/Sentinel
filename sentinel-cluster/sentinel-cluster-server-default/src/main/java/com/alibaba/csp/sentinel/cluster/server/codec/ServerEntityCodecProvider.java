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
package com.alibaba.csp.sentinel.cluster.server.codec;

import com.alibaba.csp.sentinel.cluster.codec.request.RequestEntityDecoder;
import com.alibaba.csp.sentinel.cluster.codec.response.ResponseEntityWriter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ServerEntityCodecProvider {

    private static RequestEntityDecoder requestEntityDecoder = null;
    private static ResponseEntityWriter responseEntityWriter = null;

    static {
        resolveInstance();
    }

    private static void resolveInstance() {
        ResponseEntityWriter writer = SpiLoader.loadFirstInstance(ResponseEntityWriter.class);
        if (writer == null) {
            RecordLog.warn("[ServerEntityCodecProvider] No existing response entity writer, resolve failed");
        } else {
            responseEntityWriter = writer;
            RecordLog.info(
                "[ServerEntityCodecProvider] Response entity writer resolved: " + responseEntityWriter.getClass()
                    .getCanonicalName());
        }
        RequestEntityDecoder decoder = SpiLoader.loadFirstInstance(RequestEntityDecoder.class);
        if (decoder == null) {
            RecordLog.warn("[ServerEntityCodecProvider] No existing request entity decoder, resolve failed");
        } else {
            requestEntityDecoder = decoder;
            RecordLog.info(
                "[ServerEntityCodecProvider] Request entity decoder resolved: " + requestEntityDecoder.getClass()
                    .getCanonicalName());
        }
    }

    public static RequestEntityDecoder getRequestEntityDecoder() {
        return requestEntityDecoder;
    }

    public static ResponseEntityWriter getResponseEntityWriter() {
        return responseEntityWriter;
    }

    private ServerEntityCodecProvider() {}
}
