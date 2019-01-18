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
package com.alibaba.csp.sentinel.cluster.server.codec.registry;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.codec.EntityWriter;

import io.netty.buffer.ByteBuf;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ResponseDataWriterRegistry {

    private static final Map<Integer, EntityWriter<Object, ByteBuf>> WRITER_MAP = new HashMap<>();

    public static <T> boolean addWriter(int type, EntityWriter<T, ByteBuf> writer) {
        if (WRITER_MAP.containsKey(type)) {
            return false;
        }
        WRITER_MAP.put(type, (EntityWriter<Object, ByteBuf>)writer);
        return true;
    }

    public static EntityWriter<Object, ByteBuf> getWriter(int type) {
        return WRITER_MAP.get(type);
    }

    public static boolean remove(int type) {
        return WRITER_MAP.remove(type) != null;
    }
}
