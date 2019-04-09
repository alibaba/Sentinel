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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;

/**
 * Metrics for frequent ("hot spot") parameters.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParameterMetric {

	Object LOCK = new Object();

	private Map<ParamFlowRule, CacheMap<Object, AtomicReference<Long>>> rulePassTimeCounter = new HashMap<ParamFlowRule, CacheMap<Object, AtomicReference<Long>>>();

	private Map<Integer, CacheMap<Object, AtomicInteger>> threadCountMap = new HashMap<Integer, CacheMap<Object, AtomicInteger>>();

	public Map<Integer, CacheMap<Object, AtomicInteger>> getThreadCountMap() {
		return threadCountMap;
	}

	public CacheMap<Object, AtomicReference<Long>> getRulePassTimeCounter(ParamFlowRule rule) {
		return rulePassTimeCounter.get(rule);
	}

	public Map<ParamFlowRule, CacheMap<Object, AtomicReference<Long>>> getRuleCounterMap() {
		return rulePassTimeCounter;
	}

	public synchronized void clear() {
		threadCountMap.clear();
		rulePassTimeCounter.clear();
	}

	public void initialize(ParamFlowRule rule) {
		if (!rulePassTimeCounter.containsKey(rule)) {
			synchronized (LOCK) {
				if (rulePassTimeCounter.get(rule) == null) {
					int max = 4000 * rule.getDurationInSec() > 2000000 ? 200000 : 4000 * rule.getDurationInSec();
					rulePassTimeCounter.put(rule,
							new ConcurrentLinkedHashMapWrapper<Object, AtomicReference<Long>>(max));
				}
			}
		}

		if (!threadCountMap.containsKey(rule.getParamIdx())) {
			synchronized (LOCK) {
				if (threadCountMap.get(rule.getParamIdx()) == null) {
					threadCountMap.put(rule.getParamIdx(),
							new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(THREAD_COUNT_MAX_CAPACITY));
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void decreaseThreadCount(Object... args) {
		if (args == null) {
			return;
		}

		try {
			for (int index = 0; index < args.length; index++) {
				CacheMap<Object, AtomicInteger> threadCount = threadCountMap.get(index);
				if (threadCount == null) {
					continue;
				}

				Object arg = args[index];
				if (arg == null) {
					continue;
				}
				if (Collection.class.isAssignableFrom(arg.getClass())) {

					for (Object value : ((Collection) arg)) {
						AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
						if (oldValue != null) {
							int currentValue = oldValue.decrementAndGet();
							if (currentValue <= 0) {
								threadCount.remove(value);
							}
						}

					}
				} else if (arg.getClass().isArray()) {
					int length = Array.getLength(arg);
					for (int i = 0; i < length; i++) {
						Object value = Array.get(arg, i);
						AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
						if (oldValue != null) {
							int currentValue = oldValue.decrementAndGet();
							if (currentValue <= 0) {
								threadCount.remove(value);
							}
						}

					}
				} else {
					AtomicInteger oldValue = threadCount.putIfAbsent(arg, new AtomicInteger());
					if (oldValue != null) {
						int currentValue = oldValue.decrementAndGet();
						if (currentValue <= 0) {
							threadCount.remove(arg);
						}
					}

				}

			}
		} catch (Throwable e) {
			RecordLog.warn("[ParameterMetric] Param exception", e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void addThreadCount(Object... args) {
		if (args == null) {
			return;
		}

		try {
			for (int index = 0; index < args.length; index++) {
				CacheMap<Object, AtomicInteger> threadCount = threadCountMap.get(index);
				if (threadCount == null) {
					continue;
				}

				Object arg = args[index];

				if (arg == null) {
					continue;
				}

				if (Collection.class.isAssignableFrom(arg.getClass())) {
					for (Object value : ((Collection) arg)) {
						AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
						if (oldValue != null) {
							oldValue.incrementAndGet();
						} else {
							threadCount.put(value, new AtomicInteger(1));
						}

					}
				} else if (arg.getClass().isArray()) {
					int length = Array.getLength(arg);
					for (int i = 0; i < length; i++) {
						Object value = Array.get(arg, i);
						AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
						if (oldValue != null) {
							oldValue.incrementAndGet();
						} else {
							threadCount.put(value, new AtomicInteger(1));
						}

					}
				} else {
					AtomicInteger oldValue = threadCount.putIfAbsent(arg, new AtomicInteger());
					if (oldValue != null) {
						oldValue.incrementAndGet();
					} else {
						threadCount.put(arg, new AtomicInteger(1));
					}

				}

			}

		} catch (Throwable e) {
			RecordLog.warn("[ParameterMetric] Param exception", e);
		}
	}

	public long getThreadCount(int index, Object value) {
		if (threadCountMap.get(index) == null) {
			return 0;
		}

		AtomicInteger count = threadCountMap.get(index).get(value);
		return count == null ? 0L : count.get();
	}

	private static final long THREAD_COUNT_MAX_CAPACITY = 4000;

}
