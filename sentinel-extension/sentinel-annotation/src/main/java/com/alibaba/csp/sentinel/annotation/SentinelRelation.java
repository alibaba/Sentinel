package com.alibaba.csp.sentinel.annotation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.entry.EntryBehavior;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;

public class SentinelRelation {

	private SentinelEntry sentinelEntry;
	
	private volatile Map<String , SentinelBehavior> methonSentinelEntry = new ConcurrentHashMap<String, SentinelBehavior>();
	
	public void setSentinelEntry( SentinelEntry sentinelEntry ) {
		this.sentinelEntry = sentinelEntry;
	}
	
	public void addMethonSentinelEntry(String methonName , SentinelBehavior sentinelBehavior) {
		methonSentinelEntry.put(methonName, sentinelBehavior);
	}
	
	public EntryBehavior getEntryBehavior(SentinelRelationAcquisition acquisition , EntryType entryType) {
	
		String methonName = acquisition.getMethodName();
		SentinelBehavior sentinelBehavior = methonSentinelEntry.get( methonName );
		if( sentinelBehavior == null ) {
			sentinelBehavior = new SentinelBehavior(this.sentinelEntry, acquisition.getMethodSentinelEntry() ,entryType);
			addMethonSentinelEntry(methonName, sentinelBehavior );
		}
		return sentinelBehavior.getEntryBehavior();
	}
	
}
