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

/**
 * Metrics data for a specific resource at given {@code timestamp}.
 *
 * @author jialiang.linjl
 * @author Carpenter Lee
 */
public class MetricNode {

    private long timestamp;
    private long passQps;
    private long blockQps;
    private long successQps;
    private long exceptionQps;
    private long rt;

    /**
     * @since 1.5.0
     */
    private long occupiedPassQps;

    private String resource;

    public long getTimestamp() {
        return timestamp;
    }

    public long getOccupiedPassQps() {
        return occupiedPassQps;
    }

    public void setOccupiedPassQps(long occupiedPassQps) {
        this.occupiedPassQps = occupiedPassQps;
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

    public long getPassQps() {
        return passQps;
    }

    public void setPassQps(long passQps) {
        this.passQps = passQps;
    }

    public long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(long blockQps) {
        this.blockQps = blockQps;
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
        return "MetricNode{" + "timestamp=" + timestamp + ", passQps=" + passQps + ", blockQps=" + blockQps
            + ", successQps=" + successQps + ", exceptionQps=" + exceptionQps + ", rt=" + rt
            + ", occupiedPassQps=" + occupiedPassQps + ", resource='"
            + resource + '\'' + '}';
    }

    /**
     * To formatting string. All "|" in {@link #resource} will be replaced with
     * "_", format is: <br/>
     * <code>
     * timestamp|resource|passQps|blockQps|successQps|exceptionQps|rt|occupiedPassQps
     * </code>
     *
     * @return string format of this.
     */
    public String toThinString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append("|");
        String legalName = resource.replaceAll("\\|", "_");
        sb.append(legalName).append("|");
        sb.append(passQps).append("|");
        sb.append(blockQps).append("|");
        sb.append(successQps).append("|");
        sb.append(exceptionQps).append("|");
        sb.append(rt).append("|");
        sb.append(occupiedPassQps);
        return sb.toString();
    }

    /**
     * Parse {@link MetricNode} from thin string, see {@link #toThinString()}
     *
     * @param line
     * @return
     */
    public static MetricNode fromThinString(String line) {
        MetricNode node = new MetricNode();
        String[] strs = line.split("\\|");
        node.setTimestamp(Long.parseLong(strs[0]));
        node.setResource(strs[1]);
        node.setPassQps(Long.parseLong(strs[2]));
        node.setBlockQps(Long.parseLong(strs[3]));
        node.setSuccessQps(Long.parseLong(strs[4]));
        node.setExceptionQps(Long.parseLong(strs[5]));
        node.setRt(Long.parseLong(strs[6]));
        if (strs.length == 8) {
            node.setOccupiedPassQps(Long.parseLong(strs[7]));
        }
        return node;
    }

    /**
     * To formatting string. All "|" in {@link MetricNode#resource} will be
     * replaced with "_", format is: <br/>
     * <code>
     * timestamp|yyyy-MM-dd HH:mm:ss|resource|passQps|blockQps|successQps|exceptionQps|rt|occupiedPassQps\n
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
        sb.append(getPassQps()).append("|");
        sb.append(getBlockQps()).append("|");
        sb.append(getSuccessQps()).append("|");
        sb.append(getExceptionQps()).append("|");
        sb.append(getRt()).append("|");
        sb.append(getOccupiedPassQps());
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
        node.setPassQps(Long.parseLong(strs[3]));
        node.setBlockQps(Long.parseLong(strs[4]));
        node.setSuccessQps(Long.parseLong(strs[5]));
        node.setExceptionQps(Long.parseLong(strs[6]));
        node.setRt(Long.parseLong(strs[7]));
        if (strs.length == 9) {
            node.setOccupiedPassQps(Long.parseLong(strs[8]));
        }
        return node;
    }

}
