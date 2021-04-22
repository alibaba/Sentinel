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

import static com.alibaba.csp.sentinel.transport.message.HeartbeatMessageKeyConstants.HOST_NAME;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Test;

/**
 * @author wxq
 * @since 1.8.2
 */
public class AbstractHeartbeatMessageTest {

	@Test
	public void testBeforeGet() {
		HeartbeatMessage heartbeatMessage = new BeforeGetHeartbeatMessageTest();
		assertEquals("1", heartbeatMessage.get().get("count"));
		assertEquals("2", heartbeatMessage.get().get("count"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testModify() {
		HeartbeatMessage heartbeatMessage = new EmptyHeartbeatMessageTest();
		heartbeatMessage.get().put(HOST_NAME, "error host name");
	}
	
	/**
	 * @author wxq
	 * @since 1.8.2
	 */
	private static class EmptyHeartbeatMessageTest extends AbstractHeartbeatMessage {
		
	}
	
	/**
	 * @author wxq
	 * @since 1.8.2
	 */
	private static class BeforeGetHeartbeatMessageTest extends AbstractHeartbeatMessage {

		public BeforeGetHeartbeatMessageTest() {
			AtomicInteger atomicInteger = new AtomicInteger();
			Supplier<String> countValueSupplier = () -> String.valueOf(atomicInteger.getAndIncrement());
			this.registerInformationSupplier("count", countValueSupplier);
		}
		
		@Override
		protected void beforeGet() {
			this.refresh("count");
		}
		
	}

}
