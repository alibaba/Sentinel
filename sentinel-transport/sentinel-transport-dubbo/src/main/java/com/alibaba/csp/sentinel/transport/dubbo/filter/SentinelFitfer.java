package com.alibaba.csp.sentinel.transport.dubbo.filter;

import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.entry.SentinelEntry;
import com.alibaba.csp.sentinel.annotation.utils.EntryUtils;
import com.alibaba.csp.sentinel.annotation.utils.ReflectUtils;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.config.invoker.DelegateProviderMetaDataInvoker;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

@Activate(group = "provider")
public class SentinelFitfer implements Filter {

	private ConcurrentHashMap<String, ConcurrentHashMap<String, SentinelEntry>> sentinel = new ConcurrentHashMap<String, ConcurrentHashMap<String, SentinelEntry>>();

	@SuppressWarnings("unchecked")
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		
		if( !( invoker instanceof DelegateProviderMetaDataInvoker)) {
			return invoker.invoke(invocation);
		}
		DelegateProviderMetaDataInvoker<Object> dpmdInvoker = (DelegateProviderMetaDataInvoker<Object>)invoker;
		
		String id         = dpmdInvoker.getMetadata().getId();
		ConcurrentHashMap<String, SentinelEntry> methodSentinel = sentinel.get(id);
		if(methodSentinel == null) {
			ConcurrentHashMap<String, SentinelEntry>  newMap = new ConcurrentHashMap<String, SentinelEntry>();
			ConcurrentHashMap<String, SentinelEntry>  oldMap = sentinel.putIfAbsent(id, newMap);
			if(oldMap != null) {
				methodSentinel = oldMap;
			}else {
				methodSentinel = newMap;
			}
		}
		String methodName = invocation.getMethodName();
		
		SentinelEntry sentinelEntry = methodSentinel.get(methodName);
		if(sentinelEntry == null) {
			sentinelEntry = ReflectUtils.getSentinelEntry(dpmdInvoker.getMetadata().getRef().getClass(), methodName, invocation.getParameterTypes());
			methodSentinel.put(methodName, sentinelEntry);
		}
		
		if(EntryUtils.isNormalSentinelEntry(sentinelEntry)) {
			return invoker.invoke(invocation);
		}
		
		Entry entry = null;
		try {
			entry = SphU.entry( sentinelEntry.getName() , EntryType.IN);
			return invoker.invoke(invocation);
		} catch (BlockException e) {
            throw new SentinelRpcException(e);
        } catch (RpcException e) {
            Tracer.trace(e);
            throw e;
        } finally {
			if (entry != null) {
			    entry.exit();
			  }
		}
		
	}

}
