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
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterConstants {

    public static final int MSG_TYPE_PING = 0;
    public static final int MSG_TYPE_FLOW = 1;
    public static final int MSG_TYPE_PARAM_FLOW = 2;

    public static final int RESPONSE_STATUS_BAD = -1;
    public static final int RESPONSE_STATUS_OK = 0;

    public static final int PARAM_TYPE_INTEGER = 0;
    public static final int PARAM_TYPE_LONG = 1;
    public static final int PARAM_TYPE_BYTE = 2;
    public static final int PARAM_TYPE_DOUBLE = 3;
    public static final int PARAM_TYPE_FLOAT = 4;
    public static final int PARAM_TYPE_SHORT = 5;
    public static final int PARAM_TYPE_BOOLEAN = 6;
    public static final int PARAM_TYPE_STRING = 7;

    public static final int DEFAULT_CLUSTER_SERVER_PORT = 18730;
    public static final int DEFAULT_REQUEST_TIMEOUT = 20;
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000;

    private ClusterConstants() {}
}
