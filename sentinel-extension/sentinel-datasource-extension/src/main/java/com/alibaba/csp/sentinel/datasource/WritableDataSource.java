/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource;

import java.lang.reflect.Method;
import java.util.List;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * datasource can be written to the target
 * and datasource also be registered to the ModifyRulesCommandHandler 
 *
 * @param <S> source data type
 * @param <T> target data type
 * @author Carpenter Lee
 */
public class WritableDataSource<S, T> {

	private final ConfigParser<S, T> parser;
	private final DataSource<?, S> dataSource;
	
    public WritableDataSource(ConfigParser<S, T> configParser, DataSource<?, S> dataSource, Class<?> type) {
    	this.dataSource = dataSource;
    	this.parser = configParser;
    	this.registerDataSource(type);
    }
    
    public T parserConfig(S conf) throws Exception {
        T value = parser.parse(conf);
        return value;
    } 
    
    @SuppressWarnings("unchecked")
	private void registerDataSource(Class<?> type) {
    	try {
        	//获取类
    		Class<?> handleClass = Class.forName("com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler");
    		if (type == FlowRule.class) {
        		Method method = handleClass.getMethod("registerFlowDataSource", DataSource.class);
            	method.invoke(null, (DataSource<?, List<FlowRule>>)dataSource);
    		} else if (type == AuthorityRule.class) {
    			Method method = handleClass.getMethod("registerAuthorityDataSource", DataSource.class);
            	method.invoke(null, (DataSource<?, List<AuthorityRule>>)dataSource);
    		} else if (type == DegradeRule.class) {
    			Method method = handleClass.getMethod("registerDegradeDataSource", DataSource.class);
            	method.invoke(null, (DataSource<?, List<DegradeRule>>)dataSource);
    		} else if (type == SystemRule.class) {
    			Method method = handleClass.getMethod("registerSystemDataSource", DataSource.class);
            	method.invoke(null, (DataSource<?, List<SystemRule>>)dataSource);
    		} 
    	} catch (Exception e) {
    		RecordLog.info("registerDataSource exception", e);
    		return;
    	}
    }


}
