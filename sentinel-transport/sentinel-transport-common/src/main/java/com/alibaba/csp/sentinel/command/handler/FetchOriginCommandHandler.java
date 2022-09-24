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

import java.util.Map.Entry;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

/**
 * @author qinan.qn
 */
@CommandMapping(name = "origin", desc = "get origin clusterNode by id, request param: id={resourceName}")
public class FetchOriginCommandHandler implements CommandHandler<String> {

    private final static String FORMAT = "%-4s%-80s%-10s%-10s%-11s%-9s%-6s%-10s%-11s%-9s";
    private final static int MAX_LEN = 79;

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        StringBuilder sb = new StringBuilder();
        String name = request.getParam("id");

        ClusterNode cNode = null;

        boolean exactly = false;
        for (Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
            if (e.getKey().getName().equals(name)) {
                cNode = e.getValue();
                sb.append("id: ").append(e.getKey().getShowName()).append("\n");
                sb.append("\n");
                exactly = true;
                break;
            }
        }

        if (!exactly) {
            for (Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
                if (e.getKey().getName().indexOf(name) > 0) {
                    cNode = e.getValue();
                    sb.append("id: ").append(e.getKey().getShowName()).append("\n");
                    sb.append("\n");
                    break;
                }
            }
        }

        if (cNode == null) {
            return CommandResponse.ofSuccess("Not find cNode with id " + name);
        }
        int i = 0;
        int nameLength = 0;
        for (Entry<String, StatisticNode> e : cNode.getOriginCountMap().entrySet()) {
            int l = e.getKey().length();
            if (l > nameLength) {
                nameLength = l;
            }
            if (++i == 120) {
                break;
            }
        }
        nameLength = nameLength > MAX_LEN ? MAX_LEN : nameLength;
        String format = FORMAT.replaceAll("80", String.valueOf(nameLength + 1));
        i = 0;
        sb.append(String
            .format(format, "idx", "origin", "threadNum", "passQps", "blockQps", "totalQps", "aRt", "1m-pass",
                "1m-block", "1m-total")).append("\n");

        for (Entry<String, StatisticNode> e : cNode.getOriginCountMap().entrySet()) {
            StatisticNode node = e.getValue();
            String id = e.getKey();
            int lenNum = (int)Math.ceil((double)id.length() / nameLength) - 1;
            sb.append(String
                .format(format, i + 1, lenNum == 0 ? id : id.substring(0, nameLength), node.curThreadNum(),
                    node.passQps(), node.blockQps(), node.totalQps(), node.avgRt(),
                    node.totalRequest() - node.blockRequest(), node.blockRequest(), node.totalRequest()))
                .append("\n");
            for (int j = 1; j <= lenNum; ++j) {
                int start = nameLength * j;
                int end = j == lenNum ? id.length() : nameLength * (j + 1);
                sb.append(String
                    .format(format, "", id.substring(start, end), "", "", "", "", "", "", "", "", "", "", "", ""))
                    .append("\n");
            }
            if (++i == 30) {
                break;
            }

        }

        return CommandResponse.ofSuccess(sb.toString());
    }
}
