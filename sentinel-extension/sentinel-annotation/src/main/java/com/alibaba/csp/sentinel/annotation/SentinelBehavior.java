package com.alibaba.csp.sentinel.annotation;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.entry.EntryBehavior;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;
import com.alibaba.csp.sentinel.annotation.utils.EntryUtils;
import com.alibaba.csp.sentinel.slots.block.BlockException;

public class SentinelBehavior {

	private SentinelEntry classSentinelEntry; 
	
	private SentinelEntry methonSentinelEntry;
	
	private EntryType entryType;
	
	public SentinelBehavior(SentinelEntry classSentinelEntry , SentinelEntry methonSentinelEntry ,EntryType entryType) {
		this.classSentinelEntry = classSentinelEntry;
		this.methonSentinelEntry = methonSentinelEntry;
		this.entryType = entryType;
	}
	
	public EntryBehavior getEntryBehavior() {
		EntryBehavior entryBehavior = new EntryBehavior(getEntry(classSentinelEntry), classSentinelEntry, null);
		return new EntryBehavior(getEntry(methonSentinelEntry) , methonSentinelEntry ,entryBehavior);
	}
	
	private Entry getEntry(SentinelEntry sentinelEntry)  {
		if ( !EntryUtils.isNormalSentinelEntry(sentinelEntry)) {
			try {
				return SphU.entry( sentinelEntry.getName() , entryType , sentinelEntry.getCount() );
			} catch (BlockException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
}
