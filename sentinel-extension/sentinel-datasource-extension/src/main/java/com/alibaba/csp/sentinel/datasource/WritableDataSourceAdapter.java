package com.alibaba.csp.sentinel.datasource;

import java.lang.reflect.Method;

import com.alibaba.csp.sentinel.log.RecordLog;

public class WritableDataSourceAdapter{
	public static void registerDataSource(WritableDataSource<?> dataSource, Class<?> type) {
    	try {
    		if (type == null) {
    			RecordLog.info("the type is null");
    			return;
    		}
    		Class<?> handleClass = Class.forName("com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler");
    		Method method = handleClass.getMethod("registerDataSource", WritableDataSource.class, Class.class);
        	method.invoke(null, dataSource, type);

    	} catch (Exception e) {
    		RecordLog.info("registerDataSource exception", e);
    	}
    }
}
