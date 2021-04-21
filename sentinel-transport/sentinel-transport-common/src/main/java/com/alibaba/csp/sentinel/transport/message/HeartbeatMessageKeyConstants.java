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
package com.alibaba.csp.sentinel.transport.message;

/**
 * When sentinel client send heartbeat to sentinel dashboard, those information should be send.
 * <p>
 * Store key in current class.
 * <p>
 * If you want to implement sentinel dashboard by self, you should handle those keys which send by sentinel client.
 *
 * @author wxq
 * @since 1.8.2
 */
public final class HeartbeatMessageKeyConstants {

    private HeartbeatMessageKeyConstants() {
    }

    /**
     * @see HeartbeatMessageSuppliers#PID_SUPPLIER for value supplier
     */
    public static final String PID = "pid";

    /**
     * @see HeartbeatMessageSuppliers#APP_NAME_SUPPLIER for value supplier
     */
    public static final String APP_NAME = "app";

    /**
     * @see HeartbeatMessageSuppliers#APP_TYPE_SUPPLIER for value supplier
     */
    public static final String APP_TYPE = "app_type";

    /**
     * @see HeartbeatMessageSuppliers#SENTINEL_VERSION_SUPPLIER for value supplier
     */
    public static final String SENTINEL_VERSION = "v";

    /**
     * @see HeartbeatMessageSuppliers#HOST_NAME_SUPPLIER for value supplier
     */
    public static final String HOST_NAME = "hostname";

    /**
     * @see HeartbeatMessageSuppliers#HEARTBEAT_CLIENT_IP_SUPPLIER for value supplier
     */
    public static final String HEARTBEAT_CLIENT_IP = "ip";

    /**
     * dynamic information.
     *
     * @see HeartbeatMessageSuppliers#PORT_SUPPLIER for value supplier
     */
    public static final String PORT = "port";

    /**
     * dynamic information.
     *
     * @see HeartbeatMessageSuppliers#CURRENT_TIME_MILLIS_SUPPLIER for value supplier
     */
    public static final String CURRENT_TIME_MILLIS = "version";

}
