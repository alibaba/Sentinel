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
package com.alibaba.csp.sentinel.eagleeye;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class EagleEyeRollingFileAppender extends EagleEyeAppender {

    private static final long LOG_FLUSH_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024; // 4KB

    private final int maxBackupIndex = 3;

    private final long maxFileSize;

    private final int bufferSize = DEFAULT_BUFFER_SIZE;

    private final String filePath;

    private final AtomicBoolean isRolling = new AtomicBoolean(false);

    private BufferedOutputStream bos = null;

    private long nextFlushTime = 0L;

    private long lastRollOverTime = 0L;

    private long outputByteSize = 0L;

    private final boolean selfLogEnabled;

    private boolean multiProcessDetected = false;

    private static final String DELETE_FILE_SUFFIX = ".deleted";

    public EagleEyeRollingFileAppender(String filePath, long maxFileSize) {
        this(filePath, maxFileSize, true);
    }

    public EagleEyeRollingFileAppender(String filePath, long maxFileSize, boolean selfLogEnabled) {
        this.filePath = filePath;
        this.maxFileSize = maxFileSize;
        this.selfLogEnabled = selfLogEnabled;
        setFile();
    }

    private void setFile() {
        try {
            File logFile = new File(filePath);
            if (!logFile.exists()) {
                File parentFile = logFile.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    doSelfLog("[ERROR] Fail to mkdirs: " + parentFile.getAbsolutePath());
                    return;
                }
                try {
                    if (!logFile.createNewFile()) {
                        doSelfLog("[ERROR] Fail to create file, it exists: " + logFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    doSelfLog(
                        "[ERROR] Fail to create file: " + logFile.getAbsolutePath() + ", error=" + e.getMessage());
                }
            }
            if (!logFile.isFile() || !logFile.canWrite()) {
                doSelfLog("[ERROR] Invalid file, exists=" + logFile.exists() + ", isFile=" + logFile.isFile()
                    + ", canWrite=" + logFile.canWrite() + ", path=" + logFile.getAbsolutePath());
                return;
            }
            FileOutputStream ostream = new FileOutputStream(logFile, true);
            // true
            // O_APPEND
            this.bos = new BufferedOutputStream(ostream, bufferSize);
            this.lastRollOverTime = System.currentTimeMillis();
            this.outputByteSize = logFile.length();
        } catch (Throwable e) {
            doSelfLog("[ERROR] Fail to create file to write: " + filePath + ", error=" + e.getMessage());
        }
    }

    @Override
    public void append(String log) {
        BufferedOutputStream bos = this.bos;
        if (bos != null) {
            try {
                waitUntilRollFinish();

                byte[] bytes = log.getBytes(EagleEye.DEFAULT_CHARSET);
                int len = bytes.length;
                if (len > DEFAULT_BUFFER_SIZE && this.multiProcessDetected) {
                    len = DEFAULT_BUFFER_SIZE;
                    bytes[len - 1] = '\n';
                }
                bos.write(bytes, 0, len);
                outputByteSize += len;

                if (outputByteSize >= maxFileSize) {
                    rollOver();
                } else {
                    if (System.currentTimeMillis() >= nextFlushTime) {
                        flush();
                    }
                }
            } catch (Exception e) {
                doSelfLog("[ERROR] fail to write log to file " + filePath + ", error=" + e.getMessage());
                close();
                setFile();
            }
        }
    }

    @Override
    public void flush() {
        final BufferedOutputStream bos = this.bos;
        if (bos != null) {
            try {
                bos.flush();
                nextFlushTime = System.currentTimeMillis() + LOG_FLUSH_INTERVAL;
            } catch (Exception e) {
                doSelfLog("[WARN] Fail to flush OutputStream: " + filePath + ", " + e.getMessage());
            }
        }
    }

    @Override
    public void rollOver() {
        final String lockFilePath = filePath + ".lock";
        final File lockFile = new File(lockFilePath);

        RandomAccessFile raf = null;
        FileLock fileLock = null;

        if (!isRolling.compareAndSet(false, true)) {
            return;
        }

        try {
            raf = new RandomAccessFile(lockFile, "rw");
            fileLock = raf.getChannel().tryLock();

            if (fileLock != null) {
                File target;
                File file;
                final int maxBackupIndex = this.maxBackupIndex;

                reload();
                if (outputByteSize >= maxFileSize) {
                    file = new File(filePath + '.' + maxBackupIndex);
                    if (file.exists()) {
                        target = new File(filePath + '.' + maxBackupIndex + DELETE_FILE_SUFFIX);
                        if (!file.renameTo(target) && !file.delete()) {
                            doSelfLog("[ERROR] Fail to delete or rename file: " + file.getAbsolutePath() + " to "
                                + target.getAbsolutePath());
                        }
                    }

                    for (int i = maxBackupIndex - 1; i >= 1; i--) {
                        file = new File(filePath + '.' + i);
                        if (file.exists()) {
                            target = new File(filePath + '.' + (i + 1));
                            if (!file.renameTo(target) && !file.delete()) {
                                doSelfLog("[ERROR] Fail to delete or rename file: " + file.getAbsolutePath() + " to "
                                    + target.getAbsolutePath());
                            }
                        }
                    }

                    target = new File(filePath + "." + 1);

                    close();

                    file = new File(filePath);
                    if (file.renameTo(target)) {
                        doSelfLog("[INFO] File rolled to " + target.getAbsolutePath() + ", "
                            + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastRollOverTime)
                            + " minutes since last roll");
                    } else {
                        doSelfLog("[WARN] Fail to rename file: " + file.getAbsolutePath() + " to "
                            + target.getAbsolutePath());
                    }

                    setFile();
                }
            }
        } catch (IOException e) {
            doSelfLog("[ERROR] Fail rollover file: " + filePath + ", error=" + e.getMessage());
        } finally {
            isRolling.set(false);

            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e) {
                    doSelfLog("[ERROR] Fail to release file lock: " + lockFilePath + ", error=" + e.getMessage());
                }
            }

            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    doSelfLog("[WARN] Fail to close file lock: " + lockFilePath + ", error=" + e.getMessage());
                }
            }

            if (fileLock != null) {
                if (!lockFile.delete() && lockFile.exists()) {
                    doSelfLog("[WARN] Fail to delete file lock: " + lockFilePath);
                }
            }
        }
    }

    @Override
    public void close() {
        BufferedOutputStream bos = this.bos;
        if (bos != null) {
            try {
                bos.close();
            } catch (IOException e) {
                doSelfLog("[WARN] Fail to close OutputStream: " + e.getMessage());
            }
            this.bos = null;
        }
    }

    @Override
    public void reload() {
        flush();
        File logFile = new File(filePath);
        long fileSize = logFile.length();
        boolean fileNotExists = fileSize <= 0 && !logFile.exists();

        if (this.bos == null || fileSize < outputByteSize || fileNotExists) {
            doSelfLog("[INFO] Log file rolled over by outside: " + filePath + ", force reload");
            close();
            setFile();
        } else if (fileSize > outputByteSize) {
            this.outputByteSize = fileSize;
            if (!this.multiProcessDetected) {
                this.multiProcessDetected = true;
                if (selfLogEnabled) {
                    doSelfLog("[WARN] Multi-process file write detected: " + filePath);
                }
            }
        } else {

        }
    }

    @Override
    public void cleanup() {
        try {
            File logFile = new File(filePath);
            File parentDir = logFile.getParentFile();
            if (parentDir != null && parentDir.isDirectory()) {
                final String baseFileName = logFile.getName();
                File[] filesToDelete = parentDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (name != null && name.startsWith(baseFileName) && name.endsWith(DELETE_FILE_SUFFIX)) {
                            return true;
                        }
                        return false;
                    }
                });
                if (filesToDelete != null && filesToDelete.length > 0) {
                    for (File f : filesToDelete) {
                        boolean success = f.delete() || !f.exists();
                        if (success) {
                            doSelfLog("[INFO] Deleted log file: " + f.getAbsolutePath());
                        } else if (f.exists()) {
                            doSelfLog("[ERROR] Fail to delete log file: " + f.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            doSelfLog("[ERROR] Fail to cleanup log file, error=" + e.getMessage());
        }
    }

    void waitUntilRollFinish() {
        while (isRolling.get()) {
            try {
                Thread.sleep(1L);
            } catch (Exception e) {
                // quietly
            }
        }
    }

    private void doSelfLog(String log) {
        if (selfLogEnabled) {
            EagleEye.selfLog(log);
        } else {
            System.out.println("[EagleEye]" + log);
        }
    }

    @Override
    public String getOutputLocation() {
        return filePath;
    }

    @Override
    public String toString() {
        return "EagleEyeRollingFileAppender [filePath=" + filePath + "]";
    }
}
