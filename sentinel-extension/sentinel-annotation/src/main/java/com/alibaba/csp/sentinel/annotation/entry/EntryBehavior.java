package com.alibaba.csp.sentinel.annotation.entry;

import com.alibaba.csp.sentinel.Entry;

public class EntryBehavior {

	private Entry entry;
	
	private SentinelEntry sentinelEntry;
	
	private EntryBehavior parent;

	
	
	
	public EntryBehavior(Entry entry, SentinelEntry sentinelEntry, EntryBehavior parent) {
		super();
		this.entry = entry;
		this.sentinelEntry = sentinelEntry;
		this.parent = parent;
	}

	public  void exit(Throwable throwable) {
		if( !sentinelEntry.isThisThrowable(throwable.getClass()) ) {
			exit();
		}
	}
	
	public void exit() {
		if(entry != null) {
			entry.exit();
		}
		if(parent != null) {
			parent.exit();
		}
	}
	
	
	
	
}
