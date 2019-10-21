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

import com.alibaba.csp.sentinel.config.SentinelConfig;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 从指定目录下找出所有的metric文件，并按照指定时间戳进行检索，参考{@link MetricSearcher#find(long, int)}。
 * 会借助索引以提高检索效率，参考{@link MetricWriter}；还会在内部缓存上一次检索的文件指针，以便下一次顺序检索时
 * 减少读盘次数。
 *
 * @author leyou
 */
public class MetricSearcher {

    private static final Charset defaultCharset = Charset.forName(SentinelConfig.charset());
    private final MetricsReader metricsReader;

    private String baseDir;
    private String baseFileName;

    private Position lastPosition = new Position();

    /**
     * @param baseDir      metric文件所在目录
     * @param baseFileName metric文件名的关键字，比如 alihot-metrics.log
     */
    public MetricSearcher(String baseDir, String baseFileName) {
        this(baseDir, baseFileName, defaultCharset);
    }

    /**
     * @param baseDir      metric文件所在目录
     * @param baseFileName metric文件名的关键字，比如 alihot-metrics.log
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
     * 从beginTime开始，检索recommendLines条(大概)记录。同一秒中的数据是原子的，不能分割成多次查询。
     *
     * @param beginTimeMs    检索的最小时间戳
     * @param recommendLines 查询最多想得到的记录条数，返回条数会尽可能不超过这个数字。但是为保证每一秒的数据不被分割，有时候
     *                       返回的记录条数会大于该数字。
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
     * 记录上一次读取的index文件位置和数值
     */
    private static final class Position {
        String metricFileName;
        String indexFileName;
        /**
         * 索引文件内的偏移
         */
        long offsetInIndex;
        /**
         * 索引文件中offsetInIndex位置上的数字，秒数。
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
