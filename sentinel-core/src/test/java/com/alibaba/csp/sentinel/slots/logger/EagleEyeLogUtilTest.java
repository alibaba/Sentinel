package com.alibaba.csp.sentinel.slots.logger;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.log.RecordLog;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.io.FileMatchers;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

/**
 * @author Carpenter Lee
 */
public class EagleEyeLogUtilTest {

    @Test
    public void testWriteLog() throws Exception {
        EagleEyeLogUtil.log("resourceName", "BlockException", "app1", "origin", 1);

        final File file = new File(RecordLog.getLogBaseDir() + EagleEyeLogUtil.FILE_NAME);
        await().timeout(2, TimeUnit.SECONDS)
            .until(new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return file;
                }
            }, FileMatchers.anExistingFile());
    }

    @Test
    public void testChangeLogBase() throws Exception {
        String userHome = System.getProperty("user.home");
        String newLogBase = userHome + File.separator + "tmpLogDir" + System.currentTimeMillis();
        System.setProperty(LogBase.LOG_DIR, newLogBase);

        EagleEyeLogUtil.log("resourceName", "BlockException", "app1", "origin", 1);

        final File file = new File(RecordLog.getLogBaseDir() + EagleEyeLogUtil.FILE_NAME);
        await().timeout(2, TimeUnit.SECONDS)
            .until(new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return file;
                }
            }, FileMatchers.anExistingFile());
    }
}