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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenService;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServerProvider;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * Rule checker for parameter flow control.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
final class ParamFlowChecker {

	static boolean passCheck(ResourceWrapper resourceWrapper, /* @Valid */ ParamFlowRule rule, /* @Valid */ int count,
			Object... args) {
		if (args == null) {
			return true;
		}

		int paramIdx = rule.getParamIdx();
		if (args.length <= paramIdx) {
			return true;
		}

		// Get parameter value. If value is null, then pass.
		Object value = args[paramIdx];
		if (value == null) {
			return true;
		}

		if (rule.isClusterMode() && rule.getGrade() == RuleConstant.FLOW_GRADE_QPS) {
			return passClusterCheck(resourceWrapper, rule, count, value);
		}

		return passLocalCheck(resourceWrapper, rule, count, value);
	}

	private static boolean passLocalCheck(ResourceWrapper resourceWrapper, ParamFlowRule rule, int count,
			Object value) {
		try {
			if (Collection.class.isAssignableFrom(value.getClass())) {
				for (Object param : ((Collection) value)) {
					if (!passSingleValueCheck(resourceWrapper, rule, count, param)) {
						return false;
					}
				}
			} else if (value.getClass().isArray()) {
				int length = Array.getLength(value);
				for (int i = 0; i < length; i++) {
					Object param = Array.get(value, i);
					if (!passSingleValueCheck(resourceWrapper, rule, count, param)) {
						return false;
					}
				}
			} else {
				return passSingleValueCheck(resourceWrapper, rule, count, value);
			}
		} catch (Throwable e) {
			RecordLog.warn("[ParamFlowChecker] Unexpected error", e);
		}

		return true;
	}

	static boolean passDefaultCheck(ResourceWrapper resourceWrapper, ParamFlowRule rule, int acquireCount,
			Object value) {

		CacheMap<Object, AtomicInteger> qpsCounters = getHotParameters(resourceWrapper) == null ? null
				: getHotParameters(resourceWrapper).getRuleQpsCounter(rule);

		CacheMap<Object, AtomicLong> timeCounters = getHotParameters(resourceWrapper) == null ? null
				: getHotParameters(resourceWrapper).getRuleTimeCounter(rule);

		if (qpsCounters == null || timeCounters == null) {
			return true;
		}

		// 计算它应该加的值
		Set<Object> exclusionItems = rule.getParsedHotItems().keySet();
		int tokenCount = (int) rule.getCount();
		if (exclusionItems.contains(value)) {
			tokenCount = rule.getParsedHotItems().get(value);
		}
		if (tokenCount == 0) {
			return false;
		}

		int maxCount = tokenCount + rule.getBurstCount();

		// 开始更新
		while (true) {
			// Add token
			Long currentTime = TimeUtil.currentTimeMillis();

			AtomicLong lastAddTokenTime = timeCounters.putIfAbsent(value,
					new AtomicLong(currentTime));

			if (lastAddTokenTime == null) {
				qpsCounters.putIfAbsent(value, new AtomicInteger(maxCount -acquireCount));
				return true;
			}

			// 需要添加Token了
			long passTime = currentTime - lastAddTokenTime.get();
			if (passTime > rule.getDurationInSec() * 1000) {
				AtomicInteger oldQps = qpsCounters.putIfAbsent(value,
						new AtomicInteger(maxCount - acquireCount));
				if (oldQps == null) {
					// 这里可能不精确
					lastAddTokenTime.set(currentTime);
					return true;
				} else {
					Integer restQps = oldQps.get();
					Integer toAddCount = (int) ((passTime * tokenCount)/ (rule.getDurationInSec() * 1000) );
					Integer newQps = (restQps + toAddCount) > maxCount ? (maxCount - acquireCount) : (restQps + toAddCount - acquireCount);
					
					if(newQps <0){
						return false;
					}

					if (oldQps.compareAndSet(restQps, newQps)) {
						lastAddTokenTime.set(currentTime);
						return true;
					} else {
						Thread.yield();
					}
				}
			} else {
				AtomicInteger oldQps = qpsCounters.get(value);
				if (oldQps == null) {
					Thread.yield();
				} else {
					Integer oldQpsValue = oldQps.get();
					if (oldQpsValue - acquireCount >= 0) {
						if (oldQps.compareAndSet(oldQpsValue, oldQpsValue - acquireCount)) {
							return true;
						} else {
							Thread.yield();
						}
					} else {
						return false;
					}
				}
			}

		}

	}

	static boolean passRateLimiterCheck(ResourceWrapper resourceWrapper, ParamFlowRule rule, int acquireCount,
			Object value) {

		CacheMap<Object, AtomicLong> ruleCounter = getHotParameters(resourceWrapper) == null ? null
				: getHotParameters(resourceWrapper).getRuleTimeCounter(rule);
		if (ruleCounter == null) {
			return true;
		}

		Set<Object> exclusionItems = rule.getParsedHotItems().keySet();

		long addedCount = (long) rule.getCount();
		if (exclusionItems.contains(value)) {
			addedCount = rule.getParsedHotItems().get(value);
		}

		if (addedCount == 0) {
			return false;
		}

		long costTime = Math.round(1.0 * 1000 * acquireCount * rule.getDurationInSec() / addedCount);

		while (true) {
			// Add token
			long currentTime = TimeUtil.currentTimeMillis();
			Long lastPassTime = (ruleCounter.get(value) == null) ? null : ruleCounter.get(value).get();
			long expectedTime = lastPassTime == null ? (currentTime) : (lastPassTime + costTime);

			if (expectedTime <= currentTime || expectedTime - currentTime < rule.getTimeoutInMs()) {
				AtomicLong lastPastTimeRef = ruleCounter.putIfAbsent(value,
						new AtomicLong(expectedTime));
				if (lastPastTimeRef == null) {
					return true;
				}

				if (lastPastTimeRef.compareAndSet(lastPassTime, currentTime)) {
					long waitTime = expectedTime - currentTime;
					if (waitTime > 0) {
						try {
							//
							lastPastTimeRef.set(expectedTime);
							TimeUnit.MILLISECONDS.sleep(waitTime);
						} catch (InterruptedException e) {
							RecordLog.info("could not wait ", e);
						}
					}
					return true;
				} else {
					Thread.yield();
				}
			} else {
				// System.out.println("Fail: " + sb.toString());
				return false;
			}
		}

	}

	static boolean passSingleValueCheck(ResourceWrapper resourceWrapper, ParamFlowRule rule, int acuuireCount,
			Object value) {
		if (rule.getGrade() == RuleConstant.FLOW_GRADE_QPS) {
			if (rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER) {
				return passRateLimiterCheck(resourceWrapper, rule, acuuireCount, value);
			} else {
				return passDefaultCheck(resourceWrapper, rule, acuuireCount, value);
			}

		} else if (rule.getGrade() == RuleConstant.FLOW_GRADE_THREAD) {
			Set<Object> exclusionItems = rule.getParsedHotItems().keySet();
			long threadCount = getHotParameters(resourceWrapper).getThreadCount(rule.getParamIdx(), value);
			if (exclusionItems.contains(value)) {
				int itemThreshold = rule.getParsedHotItems().get(value);
				return ++threadCount <= itemThreshold;
			}
			long threshold = (long) rule.getCount();
			return ++threadCount <= threshold;
		}

		return true;
	}

	private static ParameterMetric getHotParameters(ResourceWrapper resourceWrapper) {
		// Should not be null.
		return ParamFlowSlot.getParamMetric(resourceWrapper);
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> toCollection(Object value) {
		if (value instanceof Collection) {
			return (Collection<Object>) value;
		} else if (value.getClass().isArray()) {
			List<Object> params = new ArrayList<Object>();
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) {
				Object param = Array.get(value, i);
				params.add(param);
			}
			return params;
		} else {
			return Collections.singletonList(value);
		}
	}

	private static boolean passClusterCheck(ResourceWrapper resourceWrapper, ParamFlowRule rule, int count,
			Object value) {
		try {
			Collection<Object> params = toCollection(value);

			TokenService clusterService = pickClusterService();
			if (clusterService == null) {
				// No available cluster client or server, fallback to local or
				// pass in need.
				return fallbackToLocalOrPass(resourceWrapper, rule, count, params);
			}

			TokenResult result = clusterService.requestParamToken(rule.getClusterConfig().getFlowId(), count, params);
			switch (result.getStatus()) {
			case TokenResultStatus.OK:
				return true;
			case TokenResultStatus.BLOCKED:
				return false;
			default:
				return fallbackToLocalOrPass(resourceWrapper, rule, count, params);
			}
		} catch (Throwable ex) {
			RecordLog.warn("[ParamFlowChecker] Request cluster token for parameter unexpected failed", ex);
			return fallbackToLocalOrPass(resourceWrapper, rule, count, value);
		}
	}

	private static boolean fallbackToLocalOrPass(ResourceWrapper resourceWrapper, ParamFlowRule rule, int count,
			Object value) {
		if (rule.getClusterConfig().isFallbackToLocalWhenFail()) {
			return passLocalCheck(resourceWrapper, rule, count, value);
		} else {
			// The rule won't be activated, just pass.
			return true;
		}
	}

	private static TokenService pickClusterService() {
		if (ClusterStateManager.isClient()) {
			return TokenClientProvider.getClient();
		}
		if (ClusterStateManager.isServer()) {
			return EmbeddedClusterTokenServerProvider.getServer();
		}
		return null;
	}

	private ParamFlowChecker() {
	}
}
