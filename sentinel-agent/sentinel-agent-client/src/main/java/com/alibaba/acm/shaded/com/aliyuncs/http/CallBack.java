/*
 * Copyright 2017 Alibaba Group
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

package com.alibaba.acm.shaded.com.aliyuncs.http;

/**
 * 用于异步调用时的回调逻辑
 *
 * @author VK.Gao
 * @date 2017/03/02
 */
public interface CallBack {

    /**
     * 请求失败
     *
     * @param request   封装后的请求对象，包含部分http相关信息
     * @param e         导致失败的异常
     */
    void onFailure(HttpRequest request, Exception e);

    /**
     * 收到应答
     *
     * @param request   封装后的请求对象，包含部分http相关信息
     * @param response  封装后的应答对象，包含部分http相关信息，可以调用.getBody()获取content
     */
    void onResponse(HttpRequest request, HttpResponse response);
}
