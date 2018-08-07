package com.alibaba.csp.sentinel.annotation;

import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.entry.EntryBehavior;
import com.alibaba.csp.sentinel.annotation.utils.ReflectUtils;

public class SentinelManage {

	
	private final ConcurrentHashMap<String, SentinelRelation> sentinel = new ConcurrentHashMap<String, SentinelRelation>();
	
	
	private final EntryType entryType;
	
	public SentinelManage(EntryType entryType) {
		this.entryType = entryType;
	}
	
	public EntryBehavior getEntryBehavior(SentinelRelationAcquisition acquisition) {
		SentinelRelation  sentinelRelation = getSentinelRelation(acquisition);
		return sentinelRelation.getEntryBehavior( acquisition ,  entryType );
	}
	
	private SentinelRelation getSentinelRelation( SentinelRelationAcquisition acquisition ) {
		SentinelRelation methodSentinel = sentinel.get( acquisition.getId());
		if(methodSentinel == null) {
			SentinelRelation  newMap = new SentinelRelation();
			SentinelRelation  oldMap = sentinel.putIfAbsent(acquisition.getId(), newMap);
			if(oldMap != null) {
				methodSentinel = oldMap;
			}else {
				methodSentinel = newMap;
				methodSentinel.setSentinelEntry( acquisition.getClassSentinelEntry() );
			}
		}
		return methodSentinel;
	}
	
}
