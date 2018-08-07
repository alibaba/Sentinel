package com.alibaba.csp.sentinel.transport.dubbo;

import java.util.List;

import com.alibaba.csp.sentinel.annotation.SentinelRelationAcquisition;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;
import com.alibaba.csp.sentinel.annotation.utils.ReflectUtils;
import com.alibaba.dubbo.config.invoker.DelegateProviderMetaDataInvoker;
import com.alibaba.dubbo.rpc.Invocation;

public class DubboSentinelRelationAcquisition implements SentinelRelationAcquisition {

	private DelegateProviderMetaDataInvoker<Object> dpmdInvoker;
	
	private Invocation invocation;
	
	public DubboSentinelRelationAcquisition(DelegateProviderMetaDataInvoker<Object> dpmdInvoker ,Invocation invocation) {
		this.dpmdInvoker = dpmdInvoker;
		this.invocation  = invocation;
	}
	
	@Override
	public String getId() {
		return dpmdInvoker.getMetadata().getId();
	}

	@Override
	public String getMethodName() {
		return invocation.getMethodName();
	}

	@Override
	public SentinelEntry getMethodSentinelEntry() {
		return ReflectUtils.getSentinelEntry( dpmdInvoker.getMetadata().getRef().getClass() ,invocation.getMethodName() , invocation.getParameterTypes() );
	}

	@Override
	public SentinelEntry getClassSentinelEntry() {
		return ReflectUtils.getSentinelEntry( dpmdInvoker.getMetadata().getRef().getClass() );
	}

	@Override
	public void clear() {
		
	}

	@Override
	public List<SentinelEntry> getMethodSentinelEntryList() {
		
		return null;
	}

}
