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

import java.util.List;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;

/**
 * Retrieve and aggregate {@link MetricNode} metrics.
 *
 * @author leyou
 * @author Eric Zhao
 */
@CommandMapping(name = "metric", desc = "get and aggregate metrics, accept param: startTime={startTime}&endTime={endTime}&maxLines={maxLines}&identify={resourceName}")
public class SendMetricCommandHandler implements CommandHandler<String> {

    private MetricSearcher searcher;

    private final Object lock = new Object();

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        // Note: not thread-safe.
        if (searcher == null) {
            synchronized (lock) {
                String appName = SentinelConfig.getAppName();
                if (appName == null) {
                    appName = "";
                }
                if (searcher == null) {
                    searcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
                        MetricWriter.formMetricFileName(appName, PidUtil.getPid()));
                }
            }
        }
        String startTimeStr = request.getParam("startTime");
        String endTimeStr = request.getParam("endTime");
        String maxLinesStr = request.getParam("maxLines");
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
            return CommandResponse.ofSuccess("No metrics");
        }
        StringBuilder sb = new StringBuilder();
        for (MetricNode node : list) {
            sb.append(node.toThinString()).append("\n");
        }
        return CommandResponse.ofSuccess(sb.toString());
    }
}
