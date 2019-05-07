package com.alibaba.csp.sentinel.demo.zuul.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.BlockResponse;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author yinjihuan
 */
public class MyBlockFallbackProvider implements ZuulBlockFallbackProvider {

	@Override
	public String getRoute() {
		return "*";
	}

	@Override
	public BlockResponse fallbackResponse(String route, Throwable cause) {
		if (cause instanceof BlockException) {
			return new BlockResponse(429, "限流异常", route);
		} else {
			return new BlockResponse(500, "系统错误", route);
		}
	}
}