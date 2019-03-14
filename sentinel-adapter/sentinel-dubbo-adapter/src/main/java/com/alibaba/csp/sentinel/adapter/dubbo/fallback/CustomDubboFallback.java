package com.alibaba.csp.sentinel.adapter.dubbo.fallback;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;

/**
 * 自定义返回
 * @author yunshu
 */
public class CustomDubboFallback implements DubboFallback{
    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        //判断异常中Rule是否支持可定义返回
        AbstractRule abstractRule = ex.getRule();
        if(abstractRule.getAdapterReultOn()){
            Result  result = new RpcResult();


            return result;
        }

        throw new SentinelRpcException(ex);
    }
}
