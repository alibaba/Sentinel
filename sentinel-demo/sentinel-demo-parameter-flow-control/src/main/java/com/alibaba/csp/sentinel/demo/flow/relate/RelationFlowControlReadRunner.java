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
package com.alibaba.csp.sentinel.demo.flow.relate;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangyide
 */
public class RelationFlowControlReadRunner {
	private final String resourceName;
	private int seconds;
	private final int threadCount;

	private final AtomicLong passCount = new AtomicLong();
	private final AtomicLong blockCount = new AtomicLong();

	private volatile boolean stop = false;

	public RelationFlowControlReadRunner(String resourceName, int seconds, int threadCount) {
		assertTrue(StringUtil.isNotBlank(resourceName), "Resource name cannot be empty");
		assertTrue(seconds > 0, "Time period should be positive");
		this.resourceName = resourceName;
		this.seconds = seconds;
		this.threadCount = threadCount;
	}

	private void assertTrue(boolean b, String message) {
		if (!b) {
			throw new IllegalArgumentException(message);
		}
	}

	void simulateTraffic() {
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(new RelationFlowControlReadRunner.RunTask());
			t.setName("sentinel-simulate-traffic-read-task-" + i);
			t.start();
		}
	}

	void tick() {
		Thread timer = new Thread(new RelationFlowControlReadRunner.TimerTask());
		timer.setName("sentinel-timer-read-task");
		timer.start();
	}

	private void passFor() {
		passCount.incrementAndGet();
	}

	private void blockFor() {
		blockCount.incrementAndGet();
	}

	final class RunTask implements Runnable {

		@Override
		public void run() {

			while (!stop) {
				sleep(ThreadLocalRandom.current().nextInt(0, 10));
				ContextUtil.enter(resourceName);
				Entry entry = null;
				try {
					entry = SphU.entry(resourceName);
					passFor();
				} catch (BlockException e) {
					blockFor();
				} finally {
					if (null != entry) {
						entry.exit();
					}
					ContextUtil.exit();
				}
			}
		}
	}

	private void sleep(int timeMs) {
		try {
			TimeUnit.MILLISECONDS.sleep(timeMs);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	final class TimerTask implements Runnable {
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			System.out.println("Begin to run! Go go go!");
			System.out.println("See corresponding metrics.log for accurate statistic data");
			while (!stop) {
				sleep(1000);

				// There may be a mismatch for time window of internal sliding window.
				// See corresponding `metrics.log` for accurate statistic log.

				System.out.println(String.format(
						"[%d] relate flow metrics for resource %s: "
								+ "pass count  is %d, block count: %d",
						seconds, resourceName, passCount.getAndSet(0), blockCount.getAndSet(0)));
				System.out.println("=============================");
				if (seconds-- <= 0) {
					stop = true;
				}
			}

			long cost = System.currentTimeMillis() - start;
			System.out.println("Time cost: " + cost + " ms");
			System.exit(0);
		}
	}
}
