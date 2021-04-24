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
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageKeyConstants.*;
import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageSuppliers.*;

/**
 * If you want to custom {@link HeartbeatMessage}, recommend that inherit this
 * abstract class.
 *
 * @author wxq
 * @since 1.8.2
 */
public abstract class AbstractHeartbeatMessage implements HeartbeatMessage {

	private final Map<String, String> information = new HashMap<>();

	/**
	 * wrapper {@link #information} to forbid user modify origin.
	 */
	private final Map<String, String> unmodifiableInformation = Collections.unmodifiableMap(this.information);

	private final BiConsumer<String, Supplier<String>> informationUpdater = (key, valueSupplier) -> {
		String value = valueSupplier.get();
		this.information.put(key, value);
	};

	private final Map<String, Supplier<String>> dynamicInformationSuppliers = new HashMap<>();

	public AbstractHeartbeatMessage() {
		this.setInformation(PID, PID_SUPPLIER.get());
		this.setInformation(APP_NAME, APP_NAME_SUPPLIER.get());
		// application type (since 1.6.0).
		this.setInformation(APP_TYPE, APP_TYPE_SUPPLIER.get());
		// Version of Sentinel.
		this.setInformation(SENTINEL_VERSION, SENTINEL_VERSION_SUPPLIER.get());
		this.setInformation(HOST_NAME, HOST_NAME_SUPPLIER.get());
		this.setInformation(HEARTBEAT_CLIENT_IP, HEARTBEAT_CLIENT_IP_SUPPLIER.get());
		// sentinel client's port
		this.setInformation(PORT, PORT_SUPPLIER.get());

		// Actually timestamp.
		this.registerDynamicInformationSupplier(CURRENT_TIME_MILLIS, CURRENT_TIME_MILLIS_SUPPLIER);
	}

	@Override
	public void setInformation(String key, String value) {
		this.information.put(key, value);
	}

	@Override
	public void registerDynamicInformationSupplier(String key, Supplier<String> valueSupplier) {
		this.dynamicInformationSuppliers.put(key, valueSupplier);
	}

	@Override
	public Map<String, String> get() {
		this.dynamicInformationSuppliers.forEach(this.informationUpdater);
		return this.unmodifiableInformation;
	}

}
