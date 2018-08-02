package com.alibaba.csp.sentinel.annotation.utils;

import com.alibaba.csp.sentinel.annotation.annotation.Sentinel;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;

public class EntryUtils {

	
	public static final SentinelEntry transformSentinelAnnotation(Sentinel sentinel) {
		SentinelEntry sentinelEntry = new SentinelEntry();
		String name = sentinel.name();
		if( name == null && "".equals( name)) {
			return SentinelEntry.NAME_NULL_SENTINEL;
		}
		// TODO 还需要判断，是否存在对应的Sentinel
		sentinelEntry.setName( name );
		return sentinelEntry;
	}
	
	public static final boolean isNormalSentinelEntry(SentinelEntry sentinelEntry) {
		return SentinelEntry.NOT_SENTINEL.contains(sentinelEntry);
	}
}
