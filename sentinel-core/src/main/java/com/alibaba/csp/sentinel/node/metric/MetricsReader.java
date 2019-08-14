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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads metrics data from log file.
 */
class MetricsReader {

    /**
     * Avoid OOM in any cases.
     */
    private static final int MAX_LINES_RETURN = 100000;

    private final Charset charset;

    public MetricsReader(Charset charset) {
        this.charset = charset;
    }

    /**
     * @return if should continue read, return true, else false.
     */
    boolean readMetricsInOneFileByEndTime(List<MetricNode> list, String fileName, long offset,
                                          long beginTimeMs, long endTimeMs, String identity) throws Exception {
        FileInputStream in = null;
        long beginSecond = beginTimeMs / 1000;
        long endSecond = endTimeMs / 1000;
        try {
            in = new FileInputStream(fileName);
            in.getChannel().position(offset);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                MetricNode node = MetricNode.fromFatString(line);
                long currentSecond = node.getTimestamp() / 1000;
                // currentSecond should >= beginSecond, otherwise a wrong metric file must occur
                if (currentSecond < beginSecond) {
                    return false;
                }
                if (currentSecond <= endSecond) {
                    // read all
                    if (identity == null) {
                        list.add(node);
                    } else if (node.getResource().equals(identity)) {
                        list.add(node);
                    }
                } else {
                    return false;
                }
                if (list.size() >= MAX_LINES_RETURN) {
                    return false;
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return true;
    }

    void readMetricsInOneFile(List<MetricNode> list, String fileName,
                              long offset, int recommendLines) throws Exception {
        //if(list.size() >= recommendLines){
        //    return;
        //}
        long lastSecond = -1;
        if (list.size() > 0) {
            lastSecond = list.get(list.size() - 1).getTimestamp() / 1000;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            in.getChannel().position(offset);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                MetricNode node = MetricNode.fromFatString(line);
                long currentSecond = node.getTimestamp() / 1000;

                if (list.size() < recommendLines) {
                    list.add(node);
                } else if (currentSecond == lastSecond) {
                    list.add(node);
                } else {
                    break;
                }
                lastSecond = currentSecond;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * When identity is null, all metric between the time intervalMs will be read, otherwise, only the specific
     * identity will be read.
     */
    List<MetricNode> readMetricsByEndTime(List<String> fileNames, int pos, long offset,
                                          long beginTimeMs, long endTimeMs, String identity) throws Exception {
        List<MetricNode> list = new ArrayList<MetricNode>(1024);
        if (readMetricsInOneFileByEndTime(list, fileNames.get(pos++), offset, beginTimeMs, endTimeMs, identity)) {
            while (pos < fileNames.size()
                && readMetricsInOneFileByEndTime(list, fileNames.get(pos++), 0, beginTimeMs, endTimeMs, identity)) {
            }
        }
        return list;
    }

    List<MetricNode> readMetrics(List<String> fileNames, int pos,
                                 long offset, int recommendLines) throws Exception {
        List<MetricNode> list = new ArrayList<MetricNode>(recommendLines);
        readMetricsInOneFile(list, fileNames.get(pos++), offset, recommendLines);
        while (list.size() < recommendLines && pos < fileNames.size()) {
            readMetricsInOneFile(list, fileNames.get(pos++), 0, recommendLines);
        }
        return list;
    }
}