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
package com.alibaba.csp.sentinel.dashboard.config;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class DashboardConfigTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testGetConfigStr() {
        // clear cache
        DashboardConfig.clearCache();

        // if not set, return null
        assertEquals(null, DashboardConfig.getConfigStr("a"));

        // test property
        System.setProperty("a", "111");
        assertEquals("111", DashboardConfig.getConfigStr("a"));

        // test env
        environmentVariables.set("a", "222");
        // return value in cache
        assertEquals("111", DashboardConfig.getConfigStr("a"));

        // clear cache and then test
        DashboardConfig.clearCache();
        assertEquals("222", DashboardConfig.getConfigStr("a"));
    }

    @Test
    public void testGetConfigInt() {
        // clear cache
        DashboardConfig.clearCache();

        // default value
        assertEquals(0, DashboardConfig.getConfigInt("t", 0, 10));
        DashboardConfig.clearCache();
        assertEquals(1, DashboardConfig.getConfigInt("t", 1, 10));
        
        // property, wrong format
        System.setProperty("t", "asdf");
        DashboardConfig.clearCache();
        assertEquals(0, DashboardConfig.getConfigInt("t", 0, 10));
        System.setProperty("t", "");
        DashboardConfig.clearCache();
        assertEquals(0, DashboardConfig.getConfigInt("t", 0, 10));
        
        // min value
        System.setProperty("t", "2");
        DashboardConfig.clearCache();
        assertEquals(2, DashboardConfig.getConfigInt("t", 0, 1));
        DashboardConfig.clearCache();
        assertEquals(10, DashboardConfig.getConfigInt("t", 0, 10));
        DashboardConfig.clearCache();
        assertEquals(2, DashboardConfig.getConfigInt("t", 0, -1));
        
        // env
        environmentVariables.set("t", "20");
        DashboardConfig.clearCache();
        assertEquals(20, DashboardConfig.getConfigInt("t", 0, 10));
        
        // wrong format env var, but it will override property
        environmentVariables.set("t", "20dddd");
        DashboardConfig.clearCache();
        assertEquals(0, DashboardConfig.getConfigInt("t", 0, 10));
        
        // clear env, it will take property
        environmentVariables.set("t", "");
        DashboardConfig.clearCache();
        assertEquals(10, DashboardConfig.getConfigInt("t", 0, 10));
        DashboardConfig.clearCache();
        assertEquals(2, DashboardConfig.getConfigInt("t", 0, 1));
        
        // enable cache
        System.setProperty("t", "666");
        DashboardConfig.clearCache();
        assertEquals(666, DashboardConfig.getConfigInt("t", 0, 1));
        System.setProperty("t", "777");
        assertEquals(666, DashboardConfig.getConfigInt("t", 0, 1));
        System.setProperty("t", "555");
        assertEquals(666, DashboardConfig.getConfigInt("t", 0, 1));
    }
}
