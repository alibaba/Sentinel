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

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * source can be written to the target
 * datasource be registered to the ModifyRulesCommandHandler 
 *
 * @param <S> source data type
 * @param <T> target data type
 * @author Carpenter Lee
 */
public class WritableDataSource<S, T> {

	private ConfigParser<S, T> parser;
	private DataSource<?, S> dataSource;
	private Class<?> type;
	
    public WritableDataSource(DataSource<?, S> dataSource) {
    	this.dataSource = dataSource;
    }
    
    public WritableDataSource<S, T> setConfigParser(ConfigParser<S, T> configParser) {
    	this.parser = configParser;
        return this;
    } 
    
    public WritableDataSource<S, T> setType(Class<?> type) {
    	this.type = type;
    	this.registerDataSource();
        return this;
    } 

    public T parserConfig(S conf) throws Exception {
        T value = parser.parse(conf);
        return value;
    } 
    
	private void registerDataSource() {
    	try {
    		if (type == null) {
    			RecordLog.info("the type is null");
    			return;
    		}
    		Class<?> handleClass = Class.forName("com.alibaba.csp.sentinel.command.handler.ModifyRulesCommandHandler");
    		Method method = handleClass.getMethod("registerDataSource", this.getClass(), Class.class);
        	method.invoke(null, this, type);

    	} catch (Exception e) {
    		RecordLog.info("registerDataSource exception", e);
    	}
    }
    
    public void writeDataSource(S values) throws Exception {
    	dataSource.writeDataSource(values);
    }
}
