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
package com.alibaba.csp.sentinel.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import static com.alibaba.csp.sentinel.log.LogBase.LOG_DIR;
import static com.alibaba.csp.sentinel.log.LogBase.LOG_OUTPUT_TYPE;
import static com.alibaba.csp.sentinel.util.ConfigUtil.addSeparator;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class ConfigUtilTest {

    @Test
    public void testLoadProperties() throws IOException {

        File file = null;
        String logOutputType = "utf-8",
                dir = "/data/logs/",
                fileName = "propertiesTest.properties";
        try {
            String userDir = System.getProperty("user.dir");
            file = new File(addSeparator(userDir) + "target/classes/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(LOG_OUTPUT_TYPE + "=" + logOutputType);
            out.write(System.getProperty("line.separator"));
            out.write(LOG_DIR + "=" + dir);
            out.flush();
            out.close();

            //Load from absolutePath
            Properties properties = ConfigUtil.loadProperties(file.getAbsolutePath());
            Assert.assertTrue(logOutputType.equals(properties.getProperty(LOG_OUTPUT_TYPE)));
            Assert.assertTrue(dir.equals(properties.getProperty(LOG_DIR)));


            //Load from classPath
            properties = ConfigUtil.loadProperties(ConfigUtil.CLASSPATH_FILE_FLAG + fileName);
            Assert.assertTrue(logOutputType.equals(properties.getProperty(LOG_OUTPUT_TYPE)));
            Assert.assertTrue(dir.equals(properties.getProperty(LOG_DIR)));


            //Load from relativePath
            properties = ConfigUtil.loadProperties("target/classes/" + fileName);
            Assert.assertTrue(logOutputType.equals(properties.getProperty(LOG_OUTPUT_TYPE)));
            Assert.assertTrue(dir.equals(properties.getProperty(LOG_DIR)));

        } finally {
            if (file != null) {
                file.delete();
            }
        }

    }

    //add Jvm parameter
    //-Dcsp.sentinel.charset="UTF-16"
    //@Test
    public void testLoadPropertiesWithCustomizedCharset() throws IOException {

        String charset = "UTF-16";

        File file = null;
        String dir = "/data/logs/",
                fileName = "propertiesTest.properties";
        try {
            String userDir = System.getProperty("user.dir");
            file = new File(addSeparator(userDir) + "target/classes/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), charset);
            out.write(LOG_DIR + "=" + dir);
            out.flush();
            out.close();

            //Load from absolutePath
            Properties properties = ConfigUtil.loadProperties(file.getAbsolutePath());
            Assert.assertTrue(dir.equals(properties.getProperty(LOG_DIR)));

            //Load from classPath
            properties = ConfigUtil.loadProperties(ConfigUtil.CLASSPATH_FILE_FLAG + fileName);
            Assert.assertTrue(dir.equals(properties.getProperty(LOG_DIR)));

            //Load from relativePath
            properties = ConfigUtil.loadProperties("target/classes/" + fileName);
            Assert.assertTrue(dir.equals(properties.getProperty(LOG_DIR)));

        } finally {
            if (file != null) {
                file.delete();
            }
        }

    }

}
