/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.event.exporter;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.util.PidUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * This tool is used to export event data to disk, and it is not thread-safe.
 *
 * @author Daydreamer-ia
 */
public class EventExporter {

    private static final String CHARSET = SentinelConfig.charset();

    public static final String EVENT_BASE_DIR = LogBase.getLogBaseDir();

    public static final EventFileComparator EVENT_FILE_COMPARATOR = new EventFileComparator();

    public final String BASE_EVENT_FILE_DIR = "sentinel-event";

    public final String BASE_EVENT_FILE_NAME = "event.log";

    private static final int pid = PidUtil.getPid();

    private static EventExporter INSTANCE;

    static {
        long singleEventFileSize = SentinelConfig.singleEventFileSize();
        int totalEventFileCount = SentinelConfig.totalEventFileCount();
        INSTANCE = new EventExporter(totalEventFileCount, singleEventFileSize);
    }

    private File curExportFile;

    private FileOutputStream outEvent;

    private BufferedOutputStream outEventBuf;

    private long singleFileSize;

    private int remainFileCnt;

    private FlushEThread flushETask;

    /**
     * data buffer queue.
     */
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean started = new AtomicBoolean(false);

    private EventExporter(int remainFileCnt, long singleFileSize) {
        if (remainFileCnt <= 0) {
            // the minimum remain file count is 1
            remainFileCnt = 1;
        }
        if (singleFileSize <= 0) {
            // the minimum single file size is 1KB
            singleFileSize = 1024;
        }
        this.remainFileCnt = remainFileCnt;
        this.singleFileSize = singleFileSize;
        this.flushETask = new FlushEThread();
        started.set(true);
        this.flushETask.start();
    }

    /**
     * Write data to buffer.
     *
     * @param data the data to write
     */
    public void writeBuffer(String data) {
        this.queue.offer(data);
    }

    /**
     * Write data to file.
     *
     * @param data the data to write
     * @throws Exception if any exception occurs when writing data to file
     */
    private void write(List<String> data) throws Exception {
        // check and roll to next file if necessary
        checkAndRollToNextFile();
        // write data to file
        for (String line : data) {
            outEventBuf.write((line + "\n").getBytes(CHARSET));
        }
        outEventBuf.flush();
    }

    /**
     * Check and roll to next file if necessary.
     *
     * @throws Exception if any exception occurs when rolling to next file
     */
    private void checkAndRollToNextFile() throws Exception {
        String absoluteDir = getAbsoluteDir();
        // check and create base dir
        createDirIfNecessary(absoluteDir);
        // get remain file
        List<String> remainFile = getRemainFile(absoluteDir);
        // check file count
        if (remainFile.size() >= this.remainFileCnt) {
            removeMoreOldFile(remainFile);
        }
        // create new file if necessary
        if (curExportFile == null) {
            createNewFile(absoluteDir, getFileName());
        } else {
            // check file size
            checkFileSizeAndRollToNextFile(absoluteDir, curExportFile, this.singleFileSize);
        }
    }

    /**
     * Create a new file.
     *
     * @param fileDir  the directory of the file
     * @param fileName the file name
     * @throws Exception if any exception occurs when creating the file
     */
    private void createNewFile(String fileDir, String fileName) throws Exception {
        outEvent = new FileOutputStream(fileDir + File.separator + fileName, true);
        outEventBuf = new BufferedOutputStream(outEvent);
        curExportFile = new File(fileName);
        RecordLog.info("[EventExporter] Create new file: " + curExportFile.getAbsolutePath());
    }

    /**
     * Remove more old files if the remain file count exceeds the limit.
     *
     * @param remainFile the remain file list
     */
    private void removeMoreOldFile(List<String> remainFile) {
        if (remainFile == null || remainFile.isEmpty() || remainFile.size() < remainFileCnt) {
            return;
        }
        Collections.sort(remainFile, EVENT_FILE_COMPARATOR);
        int removeCount = remainFile.size() - remainFileCnt;
        for (int i = 0; i <= removeCount; i++) {
            new File(remainFile.get(i)).delete();
            RecordLog.info("[EventExporter] Remove old file: " + remainFile.get(i));
        }
    }

    /**
     * Check if the file size exceeds the limit.
     *
     * @param absoluteDir    the directory of the file
     * @param file           the file to check
     * @param singleFileSize the single file size limit
     * @return true if the file size exceeds the limit, false otherwise
     */
    private void checkFileSizeAndRollToNextFile(String absoluteDir, File file, long singleFileSize) throws Exception {
        if (!file.exists() || file.length() >= singleFileSize) {
            createNewFile(absoluteDir, getFileName());
        }
    }

    private void createDirIfNecessary(String baseDirPath) {
        File baseDir = new File(baseDirPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    private List<String> getRemainFile(String baseDir) {
        List<String> list = new ArrayList<>();
        File baseFile = new File(baseDir);
        File[] files = baseFile.listFiles();
        if (files == null) {
            return list;
        }
        for (File file : files) {
            String fileName = file.getName();
            if (file.isFile()
                    && matchEventFilePattern(fileName)
                    && !fileName.endsWith(MetricWriter.METRIC_FILE_INDEX_SUFFIX)
                    && !fileName.endsWith(".lck")) {
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }

    /**
     * Get the absolute path of the event file, not including the file name.
     *
     * @return the absolute path of the event file
     */
    private String getAbsoluteDir() {
        String appName = SentinelConfig.getAppName();
        if (appName == null) {
            appName = "";
        } else {
            appName = appName.replace(".", "-");
        }
        return EVENT_BASE_DIR + File.separator + appName + File.separator + BASE_EVENT_FILE_DIR;
    }

    /**
     * !baseDir.isDirectory() || !baseDir.exists()
     * Match the event file pattern.
     *
     * @param fileName the file name to match
     * @return true if the file name matches the pattern, false otherwise
     */
    private boolean matchEventFilePattern(String fileName) {
        boolean startCheck = fileName.startsWith(BASE_EVENT_FILE_NAME);
        if (!startCheck) {
            return false;
        }
        String[] split = fileName.split("-");
        // last two pattern should be date and timestamp
        if (split.length < 2) {
            return false;
        }
        String date = split[split.length - 2];
        String timestamp = split[split.length - 1];
        return date.matches("^[1-9]\\d*$") && timestamp.matches("^[1-9]\\d*$");
    }


    /**
     * Get file name of current event file, not including the file path.
     *
     * @return the file name of current event file
     */
    private String getFileName() {
        long timeMillis = System.currentTimeMillis();
        String fileName = BASE_EVENT_FILE_NAME;
        if (LogBase.isLogNameUsePid()) {
            fileName = fileName + ".pid" + pid;
        }
        fileName = String.format(fileName + "-%s-%d", currentDate(timeMillis), timeMillis);
        return fileName;
    }

    /**
     * Get the num date of specific time.
     *
     * @param timestampMillis the timestamp in milliseconds
     * @return the num date of specific time
     */
    private long currentDate(long timestampMillis) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return dateTime.getYear() * 10000L + dateTime.getMonthValue() * 100L + dateTime.getDayOfMonth();
    }

    public void close() throws Exception {
        if (!started.get()) {
            return;
        }
        if (outEventBuf != null) {
            outEventBuf.close();
        }
        started.set(false);
        flushETask.interrupt();
    }

    /**
     * Comparator for event file.
     */
    public static class EventFileComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            String[] o1Split = o1.split("-");
            String[] o2Split = o2.split("-");
            String o1Date = o1Split[o1Split.length - 2];
            String o2Date = o2Split[o2Split.length - 2];
            int dateCompare = o1Date.compareTo(o2Date);
            if (dateCompare != 0) {
                return dateCompare;
            }
            String o1Time = o1Split[o1Split.length - 1];
            String o2Time = o2Split[o2Split.length - 1];
            return o2Time.compareTo(o1Time);
        }
    }

    /**
     * Flushes the data in the buffer to the disk.
     */
    private class FlushEThread extends Thread {

        @Override
        public void run() {
            while (started.get()) {
                try {
                    while (queue.isEmpty()) {
                        // wait for 800ms if there is no data in the buffer
                        LockSupport.parkNanos(800 * 1000000L);
                    }
                    Queue<String> queue = EventExporter.this.queue;
                    List<String> data = new ArrayList<>(queue.size());
                    while (!queue.isEmpty()) {
                        data.add(queue.poll());
                    }
                    // write data to file if there is any
                    write(data);
                } catch (Exception e) {
                    // create new file if any exception occurs
                    RecordLog.error("[EventExporter] Error when flushing data to disk", e);
                    try {
                        checkAndRollToNextFile();
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }
        }

    }

    public static EventExporter getINSTANCE() {
        return INSTANCE;
    }
}
