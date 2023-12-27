/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.service.impl.common.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * @author cdfive
 */
@Component
public class DefaultIdGenerator implements IdGenerator {

    private static final Logger log = LoggerFactory.getLogger(DefaultIdGenerator.class);

    private static long lastTimestamp = -1L;

    private long sequence = 0L;

    private long workerId = -1L;

    private long dataCenterId = 0L;

    public DefaultIdGenerator() {
        String ipAddress;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        String[] ipArray = ipAddress.split("\\.");
        this.dataCenterId = 1L;
        this.workerId = Long.valueOf(ipArray[3]).longValue();
        log.info("Init IdService by dataCenterId:{} and workerId:{}", this.dataCenterId, this.workerId);
    }

    @Override
    public Long nextLongId() {
        return getNextId();
    }

    @Override
    public String nextStringId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public synchronized long getNextId() {
        if (this.workerId == -1L) {
            throw new IllegalStateException("id service init failed");
        }
        long timestamp = genTime();
        if (timestamp < lastTimestamp) {
            try {
                throw new Exception("Clock moved backwards.  Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
            } catch (Exception e) {
                log.info("Got an exception when generate next id.", e);
            }
        }
        if (lastTimestamp == timestamp) {
            this.sequence = (this.sequence + 1L & 0xFFF);
            if (this.sequence == 0L) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            this.sequence = 0L;
        }
        lastTimestamp = timestamp;

        long nextId = timestamp - 1409030641843L << 22 | this.dataCenterId << 21 | this.workerId << 12 | this.sequence;
        return nextId;
    }

    private long genTime() {
        return System.currentTimeMillis();
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = genTime();
        while (timestamp <= lastTimestamp) {
            timestamp = genTime();
        }
        return timestamp;
    }
}
