package com.alibaba.csp.sentinel.slots.logger;

import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.log.RecordLog;
import org.hamcrest.io.FileMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

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

    //Change LogBase It is not not work when integration Testing
    //Because LogBase.LOG_DIR can be just static init for once and it will not be changed
    //@Test
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
        Assert.assertTrue(file.getAbsolutePath().startsWith(newLogBase));
        deleteLogDir(new File(RecordLog.getLogBaseDir()));
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
}