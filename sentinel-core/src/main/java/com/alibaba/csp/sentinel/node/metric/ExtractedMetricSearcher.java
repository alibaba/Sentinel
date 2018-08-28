package com.alibaba.csp.sentinel.node.metric;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class ExtractedMetricSearcher {
    /**
     * avoid OOM in any case
     */
    private static final int maxLinesReturn = 100000;
    private Charset charset;

    public ExtractedMetricSearcher(Charset charset) {
        this.charset = charset;
    }

    /**
     * @return if should continue read, return true, else false.
     */
    boolean readMetricsInOneFileByEndTime(List<MetricNode> list, String fileName,
                                          long offset, long endTimeMs, String identity) throws Exception {
        FileInputStream in = null;
        long endSecond = endTimeMs / 1000;
        try {
            in = new FileInputStream(fileName);
            in.getChannel().position(offset);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                MetricNode node = MetricNode.fromFatString(line);
                long currentSecond = node.getTimestamp() / 1000;
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
                if (list.size() >= maxLinesReturn) {
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
    List<MetricNode> readMetricsByEndTime(List<String> fileNames, int pos,
                                          long offset, long endTimeMs, String identity) throws Exception {
        List<MetricNode> list = new ArrayList<MetricNode>(1024);
        if (readMetricsInOneFileByEndTime(list, fileNames.get(pos++), offset, endTimeMs, identity)) {
            while (pos < fileNames.size()
                    && readMetricsInOneFileByEndTime(list, fileNames.get(pos++), 0, endTimeMs, identity)) {
            }
        }
        return list;
    }
}