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
package com.alibaba.csp.sentinel.log;

import org.junit.Assert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.alibaba.csp.sentinel.log.LogBase.*;
import static com.alibaba.csp.sentinel.util.ConfigUtil.addSeparator;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class LogBaseTest {



    //add Jvm parameter
    //-Dcsp.sentinel.config.file=log-propertiesTest.properties
    //-Dcsp.sentinel.log.charset="utf-8"
    //-Dcsp.sentinel.log.output.type="file"
    //@Test
    public void testLoadProperties() throws IOException {

        File file = null;
        String fileName = "log-propertiesTest.properties";
        try {
            file = new File(addSeparator(System.getProperty("user.dir")) + "target/classes/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(buildPropertyStr(LOG_DIR, "/data/logs/"));
            out.write("\n");
            out.write(buildPropertyStr(LOG_NAME_USE_PID, "true"));
            out.write("\n");
            out.write(buildPropertyStr(LOG_OUTPUT_TYPE, "console"));
            out.write("\n");
            out.write(buildPropertyStr(LOG_CHARSET, "gbk"));
            out.flush();
            out.close();

            //test will make dir
            //Assert.assertTrue(LogBase.getLogBaseDir().equals("/data/logs/"));
            Assert.assertTrue(LogBase.isLogNameUsePid());
            Assert.assertTrue(LogBase.getLogOutputType().equals("file"));
            Assert.assertTrue(LogBase.getLogCharset().equals("utf-8"));
        } finally {
            if (file != null) {
                file.delete();
            }
        }


    }


    private String buildPropertyStr(String key, String value) {
        return key + "=" + value;
    }
}
