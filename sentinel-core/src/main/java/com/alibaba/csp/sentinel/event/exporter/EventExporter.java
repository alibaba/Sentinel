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

    private File curMetricFile;

    private FileOutputStream outMetric;

    private BufferedOutputStream outMetricBuf;

    private long singleFileSize;

    private int remainFileCnt;

    public EventExporter(int remainFileCnt, long singleFileSize) {
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
    }

    /**
     * Write data to file.
     *
     * @param data the data to write
     * @throws Exception if any exception occurs when writing data to file
     */
    public void write(List<String> data) throws Exception {
        // check and roll to next file if necessary
        checkAndRollToNextFile();
        // write data to file
        for (String line : data) {
            outMetricBuf.write(line.getBytes(CHARSET));
            outMetricBuf.write('\n');
        }
        outMetricBuf.flush();
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
        if (curMetricFile == null) {
            createNewFile(absoluteDir, getFileName());
        } else {
            // check file size
            checkFileSizeAndRollToNextFile(absoluteDir, curMetricFile, this.singleFileSize);
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
        outMetric = new FileOutputStream(fileDir + File.separator + fileName, true);
        outMetricBuf = new BufferedOutputStream(outMetric);
        curMetricFile = new File(fileName);
        RecordLog.info("[EventExporter] Create new file: " + curMetricFile.getAbsolutePath());
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
        for (int i = 0; i < removeCount; i++) {
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
        String fileName = EVENT_BASE_DIR;
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
}
