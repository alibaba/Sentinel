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
package com.alibaba.csp.sentinel.dashboard.discovery;

import static org.junit.Assert.*;

import org.junit.Test;

import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;

/**
 * @author Jason Joo
 */
public class MachineInfoTest {

    @Test
    public void testHealthyAndDead() {
        System.setProperty(DashboardConfig.CONFIG_UNHEALTHY_MACHINE_MILLIS, "60000");
        System.setProperty(DashboardConfig.CONFIG_AUTO_REMOVE_MACHINE_MILLIS, "600000");
        DashboardConfig.clearCache();
        MachineInfo machineInfo = new MachineInfo();
        machineInfo.setHeartbeatVersion(1);
        machineInfo.setLastHeartbeat(System.currentTimeMillis() - 10000);
        assertTrue(machineInfo.isHealthy());
        assertFalse(machineInfo.isDead());

        machineInfo.setLastHeartbeat(System.currentTimeMillis() - 100000);
        assertFalse(machineInfo.isHealthy());
        assertFalse(machineInfo.isDead());

        machineInfo.setLastHeartbeat(System.currentTimeMillis() - 1000000);
        assertFalse(machineInfo.isHealthy());
        assertTrue(machineInfo.isDead());
    }
}
