package com.alibaba.csp.sentinel.annotation.utils;

import java.lang.reflect.Method;
import java.util.List;

import com.alibaba.csp.sentinel.annotation.annotation.Sentinel;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;

public class ReflectUtils {

	
	public final static SentinelEntry getSentinelEntry(Class<?> clazz ,String methodName, Class<?>[] parameterTypes) {
		try {
			Method method = clazz.getMethod(methodName, parameterTypes);
			Sentinel sentinelAnno = toMenthodGetSentinel(method);
			return sentinelAnno == null ? SentinelEntry.NULL_SENTINEL : EntryUtils.transformSentinelAnnotation(sentinelAnno);
		} catch (NoSuchMethodException e) {
			return SentinelEntry.EXCEPTION_SENTINEL;
		} catch (SecurityException e) {
			return SentinelEntry.EXCEPTION_SENTINEL;
		}
		
	}
	
	public final static SentinelEntry getSentinelEntry(Method method) {
		try {
		Sentinel sentinelAnno = toMenthodGetSentinel(method);
		return sentinelAnno == null ? SentinelEntry.NULL_SENTINEL : EntryUtils.transformSentinelAnnotation(sentinelAnno);
		} catch( Exception e ) {
			return SentinelEntry.EXCEPTION_SENTINEL;
		}
	}
	
	public final static SentinelEntry getSentinelEntry(Class<?> clazz) {
		try {
			Sentinel sentinelAnno = clazz.getAnnotation(Sentinel.class);
			return sentinelAnno == null ? SentinelEntry.NULL_SENTINEL : EntryUtils.transformSentinelAnnotation(sentinelAnno);
		} catch( Exception e ) {
			return SentinelEntry.EXCEPTION_SENTINEL;
		}
	}
	
	private static final Sentinel toMenthodGetSentinel(Method method) {
		return method.getAnnotation(Sentinel.class);
	}
	
	
	
	public final static List<SentinelEntry> getSentinelEntryList(Class<?> clazz){
		Method[] methods = clazz.getMethods();
		for(Method method : methods) {
			
		}
		return null;
	}
	
	
}
