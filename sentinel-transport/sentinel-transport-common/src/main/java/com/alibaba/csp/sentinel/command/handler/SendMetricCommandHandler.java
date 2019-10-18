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
package com.alibaba.csp.sentinel.command.handler;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * Retrieve and aggregate {@link MetricNode} metrics.
 * 主要是如何根据请求的开始结束时间，资源名来获取磁盘的文件，然后返回磁盘的统计信息，并记录一下当前的统计信息，防止重复发送统计数据到控制台。
 *
 * @author leyou
 * @author Eric Zhao
 */
@CommandMapping(name = "metric", desc = "get and aggregate metrics, accept param: "
    + "startTime={startTime}&endTime={endTime}&maxLines={maxLines}&identify={resourceName}")
public class SendMetricCommandHandler implements CommandHandler<String> {

    private MetricSearcher searcher;

    private final Object lock = new Object();

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        // Note: not thread-safe.
        // 不能保证线程安全，因为重排序
        if (searcher == null) {
            synchronized (lock) {
                //获取应用名
                String appName = SentinelConfig.getAppName();
                if (appName == null) {
                    appName = "";
                }
                if (searcher == null) {
                    //用来找metric文件，
                    searcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
                        MetricWriter.formMetricFileName(appName, PidUtil.getPid()));
                }
            }
        }
        //获取请求的开始结束时间和最大的行数
        String startTimeStr = request.getParam("startTime");
        String endTimeStr = request.getParam("endTime");
        String maxLinesStr = request.getParam("maxLines");
        //用来确定资源
        String identity = request.getParam("identity");
        long startTime = -1;
        int maxLines = 6000;
        if (StringUtil.isNotBlank(startTimeStr)) {
            startTime = Long.parseLong(startTimeStr);
        } else {
            return CommandResponse.ofSuccess("");
        }
        List<MetricNode> list;
        try {
            // Find by end time if set.
            if (StringUtil.isNotBlank(endTimeStr)) {
                long endTime = Long.parseLong(endTimeStr);
                //根据开始结束时间找到统计数据
                list = searcher.findByTimeAndResource(startTime, endTime, identity);
            } else {
                if (StringUtil.isNotBlank(maxLinesStr)) {
                    maxLines = Integer.parseInt(maxLinesStr);
                }
                maxLines = Math.min(maxLines, 12000);
                list = searcher.find(startTime, maxLines);
            }
        } catch (Exception ex) {
            return CommandResponse.ofFailure(new RuntimeException("Error when retrieving metrics", ex));
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        //如果identity为空就加入CPU负载和系统负载
        if (StringUtil.isBlank(identity)) {
            addCpuUsageAndLoad(list);
        }
        StringBuilder sb = new StringBuilder();
        for (MetricNode node : list) {
            sb.append(node.toThinString()).append("\n");
        }
        return CommandResponse.ofSuccess(sb.toString());
    }

    /**
     * add current cpu usage and load to the metric list.
     *
     * @param list metric list, should not be null
     */
    private void addCpuUsageAndLoad(List<MetricNode> list) {
        long time = TimeUtil.currentTimeMillis() / 1000 * 1000;
        double load = SystemRuleManager.getCurrentSystemAvgLoad();
        double usage = SystemRuleManager.getCurrentCpuUsage();
        if (load > 0) {
            MetricNode loadNode = toNode(load, time, Constants.SYSTEM_LOAD_RESOURCE_NAME);
            list.add(loadNode);
        }
        if (usage > 0) {
            MetricNode usageNode = toNode(usage, time, Constants.CPU_USAGE_RESOURCE_NAME);
            list.add(usageNode);
        }
    }

    /**
     * transfer the value to a MetricNode, the value will multiply 10000 then truncate
     * to long value, and as the {@link MetricNode#passQps}.
     * <p>
     * This is an eclectic scheme before we have a standard metric format.
     * </p>
     *
     * @param value    value to save.
     * @param ts       timestamp
     * @param resource resource name.
     * @return a MetricNode represents the value.
     */
    private MetricNode toNode(double value, long ts, String resource) {
        MetricNode node = new MetricNode();
        node.setPassQps((long)(value * 10000));
        node.setTimestamp(ts);
        node.setResource(resource);
        return node;
    }
}
