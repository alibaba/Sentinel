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

import com.alibaba.csp.sentinel.adapter.gateway.zuul.enums.BlockResponseEntryEnum;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Default Fallback provider for sentinel {@link BlockException}, {@literal *} meant for all routes.
 *
 * @author tiger
 */
public class DefaultBlockFallbackProvider implements ZuulBlockFallbackProvider {

    @Override
    public String getRoute() {
        return "*";
    }

    @Override
    public BlockResponse fallbackResponse(String route, Throwable cause) {
        String reason = "OK";
        if (cause != null && cause.getClass() != null) {
            reason = cause.getClass().getCanonicalName();
        }
        return fallbackResponse(BlockResponseEntryEnum.getEnumMap().get(reason));
    }

    public BlockResponse fallbackResponse(BlockResponseEntryEnum reason) {
        final BlockResponseEntryEnum re = reason;
        return new BlockResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return re.getHttpStatus();
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return re.getHttpStatus().value();
            }

            @Override
            public String getStatusText() throws IOException {
                return re.getHttpStatus().getReasonPhrase();
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream(re.getHttpStatus().getReasonPhrase().getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }
}
