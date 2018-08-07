package com.alibaba.csp.sentinel.transport.springmvc;

import java.util.List;

import org.springframework.web.method.HandlerMethod;

import com.alibaba.csp.sentinel.annotation.SentinelRelationAcquisition;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;
import com.alibaba.csp.sentinel.annotation.utils.ReflectUtils;

public class SpringMVCSentinelRelationAcquisition implements SentinelRelationAcquisition {

	private HandlerMethod handler;
	
	public SpringMVCSentinelRelationAcquisition(HandlerMethod handler) {
		this.handler = handler;
	}
	
	@Override
	public String getId() {
		return handler.getBeanType().getName();
	}

	@Override
	public String getMethodName() {
		return handler.getMethod().getName();
	}

	@Override
	public SentinelEntry getMethodSentinelEntry() {
		return ReflectUtils.getSentinelEntry(handler.getMethod());
	}

	@Override
	public SentinelEntry getClassSentinelEntry() {
		return ReflectUtils.getSentinelEntry(handler.getBeanType());
	}

	@Override
	public List<SentinelEntry> getMethodSentinelEntryList() {
		return null;
	}

	@Override
	public void clear() {

	}

}
