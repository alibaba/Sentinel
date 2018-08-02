package com.alibaba.csp.sentinel.annotation.utils;

import java.lang.reflect.Method;

import com.alibaba.csp.sentinel.annotation.annotation.Sentinel;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;

public class ReflectUtils {

	
	public static SentinelEntry getSentinelEntry(Class<?> clazz ,String methodName, Class<?>[] parameterTypes) {
		try {
			Method method = clazz.getMethod(methodName, parameterTypes);
			Sentinel sentinelAnno = method.getAnnotation(Sentinel.class);
			return sentinelAnno == null ? SentinelEntry.NULL_SENTINEL : EntryUtils.transformSentinelAnnotation(sentinelAnno);
		} catch (NoSuchMethodException e) {
			return SentinelEntry.NULL_SENTINEL;
		} catch (SecurityException e) {
			return SentinelEntry.NULL_SENTINEL;
		}
		
	}
	
	private static final Sentinel toMenthodGetSentinel(Method method) {
		return method.getAnnotation(Sentinel.class);
	}
}
