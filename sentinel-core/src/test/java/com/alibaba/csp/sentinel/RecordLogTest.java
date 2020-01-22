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

import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.PidUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author xuyue
 */
public class RecordLogTest {

    @Test
    public void testLogException() {
        Exception e = new Exception("ex");
        RecordLog.info("Error", e);
    }

    @Test
    public void testLogRolling() {
        int count = 1000;
        while (--count > 0) {
            RecordLog.info("Count " + count);
        }
    }

    //Change LogBase It is not not work when integration Testing
    //Because LogBase.LOG_DIR can be just static init for once and it will not be changed
    //@Test
    public void testChangeLogBase() {

        String userHome = System.getProperty("user.home");
        String newLogBase = userHome + File.separator + "tmpLogDir" + System.currentTimeMillis();
        System.setProperty(LogBase.LOG_DIR, newLogBase);

        RecordLog.info("testChangeLogBase");
        String logFileName = RecordLog.getLogBaseDir();
        Assert.assertTrue(newLogBase.equals(logFileName));
        File[] files = new File(logFileName).listFiles();
        assertTrue(files != null && files.length > 0);
        deleteLogDir(new File(newLogBase));


    }

    @Test
    public void testLogBaseDir() {
        assertTrue(RecordLog.getLogBaseDir().startsWith(System.getProperty("user.home")));
    }

    public void testLogNameNotUsePid() {
        String userHome = System.getProperty("user.home");
        String newLogBase = userHome + File.separator + "tmpLogDir" + System.currentTimeMillis();
        System.setProperty(LogBase.LOG_DIR, newLogBase);
        RecordLog.info("testLogNameNotUsePid");
        File[] files = new File(newLogBase).listFiles();
        assertTrue(files != null && files.length > 0);
        for (File f : files) {
            assertTrue(!f.getName().contains("pid" + PidUtil.getPid()));
        }
    }

    public void testLogNameUsePid() {
        String userHome = System.getProperty("user.home");
        String newLogBase = userHome + File.separator + "tmpLogDir" + System.currentTimeMillis();
        System.setProperty(LogBase.LOG_DIR, newLogBase);
        System.setProperty(LogBase.LOG_NAME_USE_PID, "true");

        RecordLog.info("testLogNameUsePid");
        File[] files = new File(newLogBase).listFiles();
        assertTrue(files != null && files.length > 0);
        for (File f : files) {
            assertTrue(f.getName().contains("pid" + PidUtil.getPid()));
        }
    }

    private void deleteLogDir(File logDirFile) {
        if (logDirFile != null && logDirFile.isDirectory()) {
            if (logDirFile.listFiles() != null) {
                for (File file : logDirFile.listFiles()) {
                    file.delete();
                }
            }
            logDirFile.delete();
        }
    }



    // Because log only writes into the file,
    // can't read the log(file conflict), so no assertion in this unit test.
    @Test
    public void testMessageFormatter() {
        RecordLog.info("1 2 {} 4 {} 6", "3", "5");
        RecordLog.info("1 2 {} 4 {} 6", "3");
        RecordLog.info("1 2 {} 4 {} 6");

        RecordLog.info("1 2 \\{} 4 {} 6", "5");
    }
}
