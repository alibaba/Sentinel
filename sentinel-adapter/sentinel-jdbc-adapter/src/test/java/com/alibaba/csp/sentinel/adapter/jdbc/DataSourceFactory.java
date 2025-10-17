/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.jdbc;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author icodening
 * @date 2022.02.09
 */
public class DataSourceFactory implements FactoryBean<DataSource> {
    private static final String KEY_DRIVER = "driverClassName";

    private String dataSourceType;
    private String url;
    private String driverClassName;
    private String urlKey;

    @Override
    public DataSource getObject() throws Exception {
        Class<?> dsClass = DataSourceFactory.class.getClassLoader().loadClass(dataSourceType);
        DataSource dataSource = (DataSource) BeanUtils.instantiateClass(dsClass);
        setValue(urlKey, url, dataSource);
        setValue(KEY_DRIVER, driverClassName, dataSource);
        return dataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    private void setValue(String propertyName, Object value, Object target) throws Exception {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, target.getClass());
        Method writeMethod = propertyDescriptor.getWriteMethod();
        writeMethod.setAccessible(true);
        writeMethod.invoke(target, value);
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

}
