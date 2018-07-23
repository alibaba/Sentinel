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
package com.alibaba.csp.sentinel.transport.command.codec;

import java.nio.charset.Charset;

/**
 * The encoder encodes an object of type {@code <R>} into byte array.
 *
 * @param <R> source type
 * @author Eric Zhao
 */
public interface Encoder<R> {

    /**
     * Check whether the encoder supports the given source type.
     *
     * @param clazz type of the class
     * @return {@code true} if supported, {@code false} otherwise
     */
    boolean canEncode(Class<?> clazz);

    /**
     * Encode the given object into a byte array with the given charset.
     *
     * @param r the object to encode
     * @param charset the charset
     * @return the encoded byte buffer
     * @throws Exception error occurs when encoding the object (e.g. IO fails)
     */
    byte[] encode(R r, Charset charset) throws Exception;

    /**
     * Encode the given object into a byte array with the default charset.
     *
     * @param r the object to encode
     * @return the encoded byte buffer, witch is already flipped.
     * @throws Exception error occurs when encoding the object (e.g. IO fails)
     */
    byte[] encode(R r) throws Exception;
}
