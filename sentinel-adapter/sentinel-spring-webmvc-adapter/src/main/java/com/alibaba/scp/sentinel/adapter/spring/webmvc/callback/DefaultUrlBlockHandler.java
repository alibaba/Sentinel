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
package com.alibaba.scp.sentinel.adapter.spring.webmvc.callback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.util.InterceptorUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * The default {@link UrlBlockHandler}.
 *
 * @author zhaoyuguang
 */
public class DefaultUrlBlockHandler implements UrlBlockHandler {

    @Override
    public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException ex)
            throws IOException {
        // Directly redirect to the default flow control (blocked) page or customized block page.
        InterceptorUtil.blockRequest(request, response);
    }
}
