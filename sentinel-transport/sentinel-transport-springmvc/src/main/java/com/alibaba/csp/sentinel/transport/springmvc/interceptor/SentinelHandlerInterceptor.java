package com.alibaba.csp.sentinel.transport.springmvc.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.SentinelManage;
import com.alibaba.csp.sentinel.annotation.entry.EntryBehavior;
import com.alibaba.csp.sentinel.transport.springmvc.SpringMVCSentinelRelationAcquisition;

public class SentinelHandlerInterceptor implements HandlerInterceptor {

	private final static ThreadLocal< EntryBehavior  >  THREAD_LOCAL = new ThreadLocal<EntryBehavior>();
	
	private final SentinelManage sentinelManage = new SentinelManage( EntryType.IN );

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if( !(handler instanceof HandlerMethod)) {
			return false;
		}
		
		SpringMVCSentinelRelationAcquisition acquisition = new SpringMVCSentinelRelationAcquisition( (HandlerMethod)handler );
		EntryBehavior entryBehavior = sentinelManage.getEntryBehavior(acquisition);
		THREAD_LOCAL.set(entryBehavior);
		
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		exit();
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		exit();
	}

	
	private void exit() {
		
		EntryBehavior entryBehavior = THREAD_LOCAL.get();
		if( entryBehavior != null) {
			entryBehavior.exit();
			THREAD_LOCAL.remove();
		}
	}
}
