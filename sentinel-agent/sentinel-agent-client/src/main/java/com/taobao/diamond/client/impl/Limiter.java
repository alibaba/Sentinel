package com.taobao.diamond.client.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.taobao.middleware.logger.Logger;

/**
 * 
 * 
 * @author Diamond
 *
 */
public class Limiter {

	static final public Logger log = LogUtils.logger(Limiter.class);
	
	private static Cache<String, RateLimiter> cache = CacheBuilder.newBuilder()
			.initialCapacity(1000).expireAfterAccess(1, TimeUnit.MINUTES)
			.build();

	// qps 5
	private static final String DEFAULT_LIMIT = "5";
	private static double limit = 5;

	static {
		try {
			String limitTimeStr = System
					.getProperty("limitTime", DEFAULT_LIMIT);
			limit = Double.parseDouble(limitTimeStr);
			log.info("limitTime:{}", limit);
		} catch (Exception e) {
			log.error("Diamond-xxx", "init limitTime fail", e);
		}
	}

	public static boolean isLimit(String access_key_id) {
		RateLimiter rateLimiter = null;
		try {
			rateLimiter = cache.get(access_key_id, new Callable<RateLimiter>() {
				@Override
				public RateLimiter call() throws Exception {
					return RateLimiter.create(limit);
				}
			});
		} catch (ExecutionException e) {
			log.error("Diamond-XXX", "create limit fail", e);
		}
		if (!rateLimiter.tryAcquire(1000,TimeUnit.MILLISECONDS)) {
			log.error("Diamond-XXX", "access_key_id:{} limited", access_key_id);
			return true;
		}
		return false;
	}

}
