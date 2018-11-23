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
package com.alibaba.csp.sentinel.cluster.client.codec.registry;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.codec.EntityDecoder;

import io.netty.buffer.ByteBuf;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ResponseDataDecodeRegistry {

    private static final Map<Integer, EntityDecoder<ByteBuf, ?>> DECODER_MAP = new HashMap<>();

    public static boolean addDecoder(int type, EntityDecoder<ByteBuf, ?> decoder) {
        if (DECODER_MAP.containsKey(type)) {
            return false;
        }
        DECODER_MAP.put(type, decoder);
        return true;
    }

    public static EntityDecoder<ByteBuf, Object> getDecoder(int type) {
        return (EntityDecoder<ByteBuf, Object>)DECODER_MAP.get(type);
    }

    public static boolean removeDecoder(int type) {
        return DECODER_MAP.remove(type) != null;
    }
}
