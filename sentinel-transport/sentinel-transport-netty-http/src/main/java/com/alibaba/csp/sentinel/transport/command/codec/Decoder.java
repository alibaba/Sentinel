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
 * The decoder decodes bytes into an object of type {@code <R>}.
 *
 * @param <R> target type
 * @author Eric Zhao
 */
public interface Decoder<R> {

    /**
     * Check whether the decoder supports the given target type.
     *
     * @param clazz type of the class
     * @return {@code true} if supported, {@code false} otherwise
     */
    boolean canDecode(Class<?> clazz);

    /**
     * Decode the given byte array into an object of type {@code R} with the default charset.
     *
     * @param bytes raw byte buffer
     * @return the decoded target object
     * @throws Exception error occurs when decoding the object (e.g. IO fails)
     */
    R decode(byte[] bytes) throws Exception;

    /**
     * Decode the given byte array into an object of type {@code R} with the given charset.
     *
     * @param bytes raw byte buffer
     * @param charset the charset
     * @return the decoded target object
     * @throws Exception error occurs when decoding the object (e.g. IO fails)
     */
    R decode(byte[] bytes, Charset charset) throws Exception;
}
