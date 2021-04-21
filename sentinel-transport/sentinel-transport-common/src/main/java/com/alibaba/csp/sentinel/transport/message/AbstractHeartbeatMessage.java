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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageKeyConstants.*;
import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageSuppliers.*;

/**
 * If you want to custom {@link HeartbeatMessage}, recommend that inherit this abstract class,
 * <p>
 * and use {@link #registerStaticInformation(String, String)} or {@link #registerDynamicInformation(String, Supplier)} to add the information you want.
 *
 * @author wxq
 * @since 1.8.2
 */
public abstract class AbstractHeartbeatMessage implements HeartbeatMessage {

    private final Map<String, String> staticMessage = new ConcurrentHashMap<>();

    private final Map<String, Supplier<String>> dynamicMessage = new ConcurrentHashMap<>();

    public AbstractHeartbeatMessage() {
        // static information
        this.registerStaticInformation(PID, PID_SUPPLIER);
        this.registerStaticInformation(APP_NAME, APP_NAME_SUPPLIER);
        // application type (since 1.6.0).
        this.registerStaticInformation(APP_TYPE, APP_TYPE_SUPPLIER);
        // Version of Sentinel.
        this.registerStaticInformation(SENTINEL_VERSION, SENTINEL_VERSION_SUPPLIER);
        this.registerStaticInformation(HOST_NAME, HOST_NAME_SUPPLIER);
        this.registerStaticInformation(HEARTBEAT_CLIENT_IP, HEARTBEAT_CLIENT_IP_SUPPLIER);

        // dynamic information
        // sentinel client's port
        this.registerDynamicInformation(PORT, PORT_SUPPLIER);
        // Actually timestamp.
        this.registerDynamicInformation(CURRENT_TIME_MILLIS, CURRENT_TIME_MILLIS_SUPPLIER);
    }

    protected void registerStaticInformation(String key, String value) {
        this.staticMessage.put(key, value);
    }

    protected void registerStaticInformation(String key, Supplier<String> valueSupplier) {
        this.registerStaticInformation(key, valueSupplier.get());
    }

    /**
     * value will be resolved every time when user use method {@link #get()}.
     */
    protected void registerDynamicInformation(String key, Supplier<String> valueSupplier) {
        this.dynamicMessage.put(key, valueSupplier);
    }

    @Override
    public Map<String, String> get() {
        Map<String, String> message = new HashMap<>(this.staticMessage.size() + this.dynamicMessage.size());
        message.putAll(this.staticMessage);

        for (Map.Entry<String, Supplier<String>> entry : this.dynamicMessage.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().get();
            message.put(key, value);
        }

        return message;
    }
}
