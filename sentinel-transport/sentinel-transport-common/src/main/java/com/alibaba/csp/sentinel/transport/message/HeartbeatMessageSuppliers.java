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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;

import java.util.function.Supplier;

/**
 * Value supplier for key in {@link HeartbeatMessageKeyConstants}.
 *
 * @author wxq
 * @since 1.8.2
 */
public final class HeartbeatMessageSuppliers {

    private HeartbeatMessageSuppliers() {
    }

    /**
     * @see HeartbeatMessageKeyConstants#PID for key
     */
    public static final Supplier<String> PID_SUPPLIER = () -> String.valueOf(PidUtil.getPid());

    /**
     * @see HeartbeatMessageKeyConstants#APP_NAME for key
     */
    public static final Supplier<String> APP_NAME_SUPPLIER = AppNameUtil::getAppName;

    /**
     * @see HeartbeatMessageKeyConstants#APP_TYPE for key
     */
    public static final Supplier<String> APP_TYPE_SUPPLIER = () -> String.valueOf(SentinelConfig.getAppType());

    /**
     * @see HeartbeatMessageKeyConstants#SENTINEL_VERSION for key
     */
    public static final Supplier<String> SENTINEL_VERSION_SUPPLIER = () -> Constants.SENTINEL_VERSION;

    /**
     * @see HeartbeatMessageKeyConstants#HOST_NAME for key
     */
    public static final Supplier<String> HOST_NAME_SUPPLIER = HostNameUtil::getHostName;

    /**
     * @see HeartbeatMessageKeyConstants#HEARTBEAT_CLIENT_IP for key
     */
    public static final Supplier<String> HEARTBEAT_CLIENT_IP_SUPPLIER = TransportConfig::getHeartbeatClientIp;

    /**
     * @see HeartbeatMessageKeyConstants#PORT for key
     */
    public static final Supplier<String> PORT_SUPPLIER = TransportConfig::getPort;

    /**
     * @see HeartbeatMessageKeyConstants#CURRENT_TIME_MILLIS for key
     */
    public static final Supplier<String> CURRENT_TIME_MILLIS_SUPPLIER = () -> String.valueOf(System.currentTimeMillis());
}
