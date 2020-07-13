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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;

import java.io.Closeable;
import java.io.IOException;

/**
 * Fall back response for {@link com.alibaba.csp.sentinel.slots.block.BlockException}
 *
 * @author tiger
 */
public interface BlockResponse extends HttpInputMessage, Closeable {


    /**
     * Return the HTTP status code of the response.
     *
     * @return the HTTP status as an HttpStatus enum value
     * @throws IOException              in case of I/O errors
     * @throws IllegalArgumentException in case of an unknown HTTP status code
     */
    HttpStatus getStatusCode() throws IOException;

    /**
     * Return the HTTP status code (potentially non-standard and not
     * resolvable through the {@link HttpStatus} enum) as an integer.
     *
     * @return the HTTP status as an integer
     * @throws IOException in case of I/O errors
     */
    int getRawStatusCode() throws IOException;

    /**
     * Return the HTTP status text of the response.
     * @return the HTTP status text
     * @throws IOException in case of I/O errors
     */
    String getStatusText() throws IOException;

    /**
     * Close this response, freeing any resources created.
     */
    @Override
    void close();

}
