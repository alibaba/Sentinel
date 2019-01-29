/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.servlet.callback;

import javax.servlet.http.HttpServletRequest;

/**
 * The origin parser parses request origin (e.g. IP, user, appName) from HTTP request.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public interface RequestOriginParser {

    /**
     * Parse the origin from given HTTP request.
     *
     * @param request HTTP request
     * @return parsed origin
     */
    String parseOrigin(HttpServletRequest request);
}
