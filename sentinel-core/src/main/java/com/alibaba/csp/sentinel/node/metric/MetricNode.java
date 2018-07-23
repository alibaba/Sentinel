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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MetricNode {

    private long timestamp;
    private long passedQps;
    private long blockedQps;
    private long successQps;
    private long exception;
    private long rt;

    private String resource;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(long successQps) {
        this.successQps = successQps;
    }

    public long getPassedQps() {
        return passedQps;
    }

    public void setPassedQps(long passedQps) {
        this.passedQps = passedQps;
    }

    public long getException() {
        return exception;
    }

    public void setException(long exception) {
        this.exception = exception;
    }

    public long getBlockedQps() {
        return blockedQps;
    }

    public void setBlockedQps(long blockedQps) {
        this.blockedQps = blockedQps;
    }

    public long getRt() {
        return rt;
    }

    public void setRt(long rt) {
        this.rt = rt;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "MetricNode{" +
            "timestamp=" + timestamp +
            ", passedQps=" + passedQps +
            ", blockedQs=" + blockedQps +
            ", successQps=" + successQps +
            ", exception=" + exception +
            ", rt=" + rt +
            ", resource='" + resource + '\'' +
            '}';
    }

    /**
     * To formatting string. All "|" in {@link #resource} will be replaced with "_", format is:
     * <br/>
     * <code>
     * timestamp|resource|passedQps|blockedQps|successQps|exception|rt
     * </code>
     *
     * @return string format of this.
     */
    public String toThinString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append("|");
        String legalName = resource.replaceAll("\\|", "_");
        sb.append(legalName).append("|");
        sb.append(passedQps).append("|");
        sb.append(blockedQps).append("|");
        sb.append(successQps).append("|");
        sb.append(exception).append("|");
        sb.append(rt);
        return sb.toString();
    }

    /**
     * Parse {@link MetricNode} from thin string, see {@link #toThinString()} ()}
     *
     * @param line
     * @return
     */
    public static MetricNode fromThinString(String line) {
        MetricNode node = new MetricNode();
        String[] strs = line.split("\\|");
        node.setTimestamp(Long.parseLong(strs[0]));
        node.setResource(strs[1]);
        node.setPassedQps(Long.parseLong(strs[2]));
        node.setBlockedQps(Long.parseLong(strs[3]));
        node.setSuccessQps(Long.parseLong(strs[4]));
        node.setException(Long.parseLong(strs[5]));
        node.setRt(Long.parseLong(strs[6]));
        return node;
    }

    /**
     * To formatting string. All "|" in {@link MetricNode#resource} will be replaced with "_", format is:
     * <br/>
     * <code>
     * timestamp|yyyy-MM-dd HH:mm:ss|resource|passedQps|blockedQps|successQps|exception|rt\n
     * </code>
     *
     * @return string format of this.
     */
    public String toFatString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder(32);
        sb.delete(0, sb.length());
        sb.append(getTimestamp()).append("|");
        sb.append(df.format(new Date(getTimestamp()))).append("|");
        String legalName = getResource().replaceAll("\\|", "_");
        sb.append(legalName).append("|");
        sb.append(getPassedQps()).append("|");
        sb.append(getBlockedQps()).append("|");
        sb.append(getSuccessQps()).append("|");
        sb.append(getException()).append("|");
        sb.append(getRt());
        sb.append('\n');
        return sb.toString();
    }

    /**
     * Parse {@link MetricNode} from fat string, see {@link #toFatString()}
     *
     * @param line
     * @return the {@link MetricNode} parsed.
     */
    public static MetricNode fromFatString(String line) {
        String[] strs = line.split("\\|");
        Long time = Long.parseLong(strs[0]);
        MetricNode node = new MetricNode();
        node.setTimestamp(time);
        node.setResource(strs[2]);
        node.setPassedQps(Long.parseLong(strs[3]));
        node.setBlockedQps(Long.parseLong(strs[4]));
        node.setSuccessQps(Long.parseLong(strs[5]));
        node.setException(Long.parseLong(strs[6]));
        node.setRt(Long.parseLong(strs[7]));
        return node;
    }

}
