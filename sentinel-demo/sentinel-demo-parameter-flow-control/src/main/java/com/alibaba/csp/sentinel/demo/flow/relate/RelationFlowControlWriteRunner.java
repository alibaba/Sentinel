package com.alibaba.csp.sentinel.demo.flow.relate;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhangyide on 2019-09-29
 */
public class RelationFlowControlWriteRunner {
	private final String resourceName;
	private int seconds;
	private final int threadCount;


	private final AtomicLong passCount = new AtomicLong();
	private final AtomicLong blockCount = new AtomicLong();

	private volatile boolean stop = false;


	public RelationFlowControlWriteRunner(String resourceName, int seconds, int threadCount) {
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
			Thread t = new Thread(new RelationFlowControlWriteRunner.RunTask());
			t.setName("sentinel-simulate-traffic-write-task-" + i);
			t.start();
		}
	}

	void tick() {
		Thread timer = new Thread(new RelationFlowControlWriteRunner.TimerTask());
		timer.setName("sentinel-timer-write-task");
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
				// 前10秒，qps=20
				if (seconds > 1190) {
					sleep(1000);
					// 10-30秒，qps=200，触发关联限流
				} else if (seconds > 1170) {
					sleep(100);
				} else {
					// 30-60秒，qps=20，关联限流解除
					sleep(1000);
				}
				ContextUtil.enter(resourceName);
				Entry entry = null;
				try {
					entry = SphU.entry(resourceName);
					passFor();
				} catch (BlockException e) {
					blockFor();
				} finally {
					if (null != entry)
						entry.exit();
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
