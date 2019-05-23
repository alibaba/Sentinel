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
package com.alibaba.csp.sentinel.cluster;

/**
 * @author jialiang.ljl
 * @since 1.4.0
 */
public final class ClusterErrorMessages {

    public static final String BAD_REQUEST = "bad request";
    public static final String UNEXPECTED_STATUS = "unexpected status";
    public static final String TOO_MANY_REQUESTS = "too many requests (client side)";
    public static final String REQUEST_TIME_OUT = "request time out";
    public static final String CLIENT_NOT_READY = "client not ready";
    public static final String NO_RULES_IN_SERVER = "no rules in token server";

    private ClusterErrorMessages() {}
}
