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

import java.util.Map;
import java.util.function.Supplier;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;

/**
 * sentinel client will send message to dashboard when send heartbeat.
 *
 * @author wxq
 * @since 1.8.2
 * @see HeartbeatMessageKeyConstants standard information's key
 */
public interface HeartbeatMessage extends Supplier<Map<String, String>> {

	/**
	 * Set the information to message. If your value will change in runtime,
	 * recommend use {@link #registerDynamicInformationSupplier(String, Supplier)}.
	 * 
	 * @see Map#put(String, String)
	 */
	void put(String key, String value);

	/**
	 * Will get a new value from value supplier in every calling of {@link #get()}.
	 * If your value is static, recommend use {@link #put(String, String)}.
	 * 
	 * @param key           information's key
	 * @param valueSupplier information's value supplier
	 * @see TransportConfig#HEARTBEAT_INTERVAL_MS the value supplier's invoke
	 *      frequency
	 */
	void registerDynamicInformationSupplier(String key, Supplier<String> valueSupplier);
}
