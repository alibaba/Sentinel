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

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author icodening
 * @date 2022.02.09
 */
public class DataInitializer implements SmartInitializingSingleton {

    private ApplicationContext applicationContext;

    private DataSource dataSource;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Resource schema = applicationContext.getResource("schema.sql");
        Resource data = applicationContext.getResource("data.sql");
        try (Connection connection = dataSource.getConnection()) {
            byte[] schemaBytes = FileCopyUtils.copyToByteArray(schema.getFile());
            byte[] dataBytes = FileCopyUtils.copyToByteArray(data.getFile());
            String schemaSQL = new String(schemaBytes);
            String dataSQL = new String(dataBytes);
            connection.createStatement().execute(schemaSQL);
            connection.createStatement().execute(dataSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
