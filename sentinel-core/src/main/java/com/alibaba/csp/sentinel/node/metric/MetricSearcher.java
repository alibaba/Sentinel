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
package com.alibaba.csp.sentinel.node.metric;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.List;

import com.alibaba.csp.sentinel.config.SentinelConfig;

/**
 *  Find all metric files from the specified directory,and retrieve it according to the specified timestamp,refer to {@link MetricSearcher#find(long, int)}.
 *  Index will be used to improve retrieval efficiency, refer to {@link MetricWriter};The last retrieved file pointer is also cached in the internal cache,
 *  in order to reduce the number of reads on the next sequential retrieval.
 * @author leyou
 */
public class MetricSearcher {

    private static final Charset defaultCharset = Charset.forName(SentinelConfig.charset());
    private final MetricsReader metricsReader;

    private String baseDir;
    private String baseFileName;

    private Position lastPosition = new Position();

    /**
     * @param baseDir      The directory in which the metric file is located
     * @param baseFileName Keywords for metric file names,such as "alihot-metrics.log"
     */
    public MetricSearcher(String baseDir, String baseFileName) {
        this(baseDir, baseFileName, defaultCharset);
    }

    /**
     * @param baseDir      The directory in which the metric file is located
     * @param baseFileName Keywords for metric file names,such as "alihot-metrics.log"
     * @param charset
     */
    public MetricSearcher(String baseDir, String baseFileName, Charset charset) {
        if (baseDir == null) {
            throw new IllegalArgumentException("baseDir can't be null");
        }
        if (baseFileName == null) {
            throw new IllegalArgumentException("baseFileName can't be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset can't be null");
        }
        this.baseDir = baseDir;
        if (!baseDir.endsWith(File.separator)) {
            this.baseDir += File.separator;
        }
        this.baseFileName = baseFileName;
        metricsReader = new MetricsReader(charset);
    }

    /**
     * Start with beginTime,retrieve recommendLines bar records.The data in the same second is atomic,can not be split into multiple queries
     * @param beginTimeMs    Minimum timestamp retrieved
     * @param recommendLines Query the maximum number of record bars you want,the number of returns will not exceed this number as much as possible.
     *                       But in order to ensure that every second of the data is not segmented,sometimes the number of record bars returned is greater than recommendLines。
     * @return
     * @throws Exception
     */
    public synchronized List<MetricNode> find(long beginTimeMs, int recommendLines) throws Exception {
        List<String> fileNames = MetricWriter.listMetricFiles(baseDir, baseFileName);
        int i = 0;
        long offsetInIndex = 0;
        if (validPosition(beginTimeMs)) {
            i = fileNames.indexOf(lastPosition.metricFileName);
            if (i == -1) {
                i = 0;
            } else {
                offsetInIndex = lastPosition.offsetInIndex;
            }
        }
        for (; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            long offset = findOffset(beginTimeMs, fileName,
                MetricWriter.formIndexFileName(fileName), offsetInIndex);
            offsetInIndex = 0;
            if (offset != -1) {
                return metricsReader.readMetrics(fileNames, i, offset, recommendLines);
            }
        }
        return null;
    }

    /**
     * Find metric between [beginTimeMs, endTimeMs], both side inclusive.
     * When identity is null, all metric between the time intervalMs will be read, otherwise, only the specific
     * identity will be read.
     */
    public synchronized List<MetricNode> findByTimeAndResource(long beginTimeMs, long endTimeMs, String identity)
        throws Exception {
        List<String> fileNames = MetricWriter.listMetricFiles(baseDir, baseFileName);
        //RecordLog.info("pid=" + pid + ", findByTimeAndResource([" + beginTimeMs + ", " + endTimeMs
        //    + "], " + identity + ")");
        int i = 0;
        long offsetInIndex = 0;
        if (validPosition(beginTimeMs)) {
            i = fileNames.indexOf(lastPosition.metricFileName);
            if (i == -1) {
                i = 0;
            } else {
                offsetInIndex = lastPosition.offsetInIndex;
            }
        } else {
            //RecordLog.info("lastPosition is invalidate, will re iterate all files, pid = " + pid);
        }

        for (; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            long offset = findOffset(beginTimeMs, fileName,
                    MetricWriter.formIndexFileName(fileName), offsetInIndex);
            offsetInIndex = 0;
            if (offset != -1) {
                return metricsReader.readMetricsByEndTime(fileNames, i, offset, beginTimeMs, endTimeMs, identity);
            }
        }
        return null;
    }

    /**
     * Record the location and value of the last read index file.
     */
    private static final class Position {
        String metricFileName;
        String indexFileName;
        /**
         * Offset in index file
         */
        long offsetInIndex;
        /**
         * Numbers at the offsetInIndex location in the index file, seconds.
         */
        long second;
    }

    /**
     * The position we cached is useful only when {@code beginTimeMs} is >= {@code lastPosition.second}
     * and the index file exists and the second we cached is same as in the index file.
     */
    private boolean validPosition(long beginTimeMs) {
        if (beginTimeMs / 1000 < lastPosition.second) {
            return false;
        }
        if (lastPosition.indexFileName == null) {
            return false;
        }
        // index file dose not exits
        if (!new File(lastPosition.indexFileName).exists()) {
            return false;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(lastPosition.indexFileName);
            in.getChannel().position(lastPosition.offsetInIndex);
            DataInputStream indexIn = new DataInputStream(in);
            // timestamp(second) in the specific position == that we cached
            return indexIn.readLong() == lastPosition.second;
        } catch (Exception e) {
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private long findOffset(long beginTime, String metricFileName,
                            String idxFileName, long offsetInIndex) throws Exception {
        lastPosition.metricFileName = null;
        lastPosition.indexFileName = null;
        if (!new File(idxFileName).exists()) {
            return -1;
        }
        long beginSecond = beginTime / 1000;
        FileInputStream in = new FileInputStream(idxFileName);
        in.getChannel().position(offsetInIndex);
        DataInputStream indexIn = new DataInputStream(in);
        long offset;
        try {
            long second;
            lastPosition.offsetInIndex = in.getChannel().position();
            while ((second = indexIn.readLong()) < beginSecond) {
                offset = indexIn.readLong();
                lastPosition.offsetInIndex = in.getChannel().position();
            }
            offset = indexIn.readLong();
            lastPosition.metricFileName = metricFileName;
            lastPosition.indexFileName = idxFileName;
            lastPosition.second = second;
            return offset;
        } catch (EOFException ignore) {
            return -1;
        } finally {
            indexIn.close();
        }
    }
}
