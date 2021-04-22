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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageKeyConstants.*;
import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageSuppliers.*;

/**
 * If you want to custom {@link HeartbeatMessage}, recommend that inherit this abstract class.
 *
 * @author wxq
 * @since 1.8.2
 */
public abstract class AbstractHeartbeatMessage implements HeartbeatMessage {

    private final Map<String, String> information = new HashMap<>();

    private final Map<String, Supplier<String>> informationSuppliers = new ConcurrentHashMap<>();

    public AbstractHeartbeatMessage() {
        this.registerInformationSupplier(PID, PID_SUPPLIER);
        this.registerInformationSupplier(APP_NAME, APP_NAME_SUPPLIER);
        // application type (since 1.6.0).
        this.registerInformationSupplier(APP_TYPE, APP_TYPE_SUPPLIER);
        // Version of Sentinel.
        this.registerInformationSupplier(SENTINEL_VERSION, SENTINEL_VERSION_SUPPLIER);
        this.registerInformationSupplier(HOST_NAME, HOST_NAME_SUPPLIER);
        this.registerInformationSupplier(HEARTBEAT_CLIENT_IP, HEARTBEAT_CLIENT_IP_SUPPLIER);
        // sentinel client's port
        this.registerInformationSupplier(PORT, PORT_SUPPLIER);
        // Actually timestamp.
        this.registerInformationSupplier(CURRENT_TIME_MILLIS, CURRENT_TIME_MILLIS_SUPPLIER);
    }

    /**
     * @param key           information's key
     * @param valueSupplier information's value supplier
     */
    protected void registerInformationSupplier(String key, Supplier<String> valueSupplier) {
        this.informationSuppliers.put(key, valueSupplier);
        this.information.put(key, valueSupplier.get());
    }

    /**
     * update the value of key from value supplier
     *
     * @param key information's key
     */
    protected void refresh(String key) {
        String newValue = this.informationSuppliers.get(key).get();
        this.information.put(key, newValue);
    }

    /**
     * subclass may use {@link #refresh(String)} in this method instead of override {@link #get()} directly.
     */
    protected void beforeGet() {

    }

    @Override
    public Map<String, String> get() {
        this.beforeGet();
        this.refresh(CURRENT_TIME_MILLIS);
        return Collections.unmodifiableMap(this.information);
    }

}
