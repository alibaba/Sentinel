package com.alibaba.csp.sentinel.annotation;

import java.util.List;

import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;

public interface SentinelRelationAcquisition {

	String getId();
	
	String getMethodName();
	
	SentinelEntry getMethodSentinelEntry();
	
	SentinelEntry getClassSentinelEntry();
	
	List<SentinelEntry> getMethodSentinelEntryList();
	
	void clear();
	
}