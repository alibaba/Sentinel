package com.alibaba.csp.sentinel.transport.dubbo.filter;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.SentinelManage;
import com.alibaba.csp.sentinel.annotation.entry.EntryBehavior;
import com.alibaba.csp.sentinel.transport.dubbo.DubboSentinelRelationAcquisition;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.config.invoker.DelegateProviderMetaDataInvoker;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

@Activate(group = "provider")
public class SentinelFitfer2 implements Filter {

	private final SentinelManage sentinelManage = new SentinelManage( EntryType.IN );
	
	@SuppressWarnings("unchecked")
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		
		if( !( invoker instanceof DelegateProviderMetaDataInvoker)) {
			return invoker.invoke(invocation);	
		}

		DubboSentinelRelationAcquisition acquisition = new DubboSentinelRelationAcquisition((DelegateProviderMetaDataInvoker<Object>)invoker , invocation); 
		EntryBehavior entryBehavior = sentinelManage.getEntryBehavior(acquisition);
		
		try {
			return invoker.invoke(invocation);
		} finally {
			if (entryBehavior != null) {
				entryBehavior.exit();
			  }
		}
		
	}

}
