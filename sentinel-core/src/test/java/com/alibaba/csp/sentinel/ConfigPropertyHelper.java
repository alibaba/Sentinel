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
package com.alibaba.csp.sentinel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.util.AppNameUtil;

/**
 * Helper class for executing a task within a config context via properties file.
 *
 * @author Eric Zhao
 */
public final class ConfigPropertyHelper {

    public static void setAppNameProperty(String appName) {
        System.setProperty(SentinelConfig.APP_NAME_PROP_KEY, appName);
    }

    public static void clearAppNameProperty() {
        System.clearProperty(SentinelConfig.APP_NAME_PROP_KEY);
    }

    public static void runWithConfig(Properties prop, String appName, Task task) throws Exception {
        if (prop == null || appName == null || "".equals(appName)) {
            throw new IllegalArgumentException("Prop and appName cannot be empty");
        }
        // Set application name property.
        setAppNameProperty(appName);
        // Save the config.
        String path = LogBase.getLogBaseDir() + appName + ".properties";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream outputStream = new FileOutputStream(file);
        prop.store(outputStream,"");
        outputStream.close();
        // Run the procedure.
        task.run();
        // Clean-up.
        file.delete();
        // Clear application name property.
        clearAppNameProperty();
    }

    public interface Task {
        void run() throws Exception;
    }

    private ConfigPropertyHelper() {}
}
