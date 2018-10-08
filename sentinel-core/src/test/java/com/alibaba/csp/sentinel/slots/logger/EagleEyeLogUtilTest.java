package com.alibaba.csp.sentinel.slots.logger;

import java.io.File;

import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.log.RecordLog;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Carpenter Lee
 */
public class EagleEyeLogUtilTest {

    @Test
    public void testWriteLog() throws Exception {
        EagleEyeLogUtil.log("resourceName", "BlockException", "app1", "origin", 1);
        Thread.sleep(1100);
        String file = RecordLog.getLogBaseDir() + EagleEyeLogUtil.FILE_NAME;
        assertTrue(new File(file).exists());
    }

    @Test
    public void testChangeLogBase() throws Exception {
        String userHome = System.getProperty("user.home");
        String newLogBase = userHome + File.separator + "tmpLogDir" + System.currentTimeMillis();
        System.setProperty(LogBase.LOG_DIR, newLogBase);

        EagleEyeLogUtil.log("resourceName", "BlockException", "app1", "origin", 1);
        Thread.sleep(1100);
        String file = RecordLog.getLogBaseDir() + EagleEyeLogUtil.FILE_NAME;
        assertTrue(new File(file).exists());
    }
}