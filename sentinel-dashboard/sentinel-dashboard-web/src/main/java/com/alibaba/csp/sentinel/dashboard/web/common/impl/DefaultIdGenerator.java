package com.alibaba.csp.sentinel.dashboard.web.common.impl;

import com.alibaba.csp.sentinel.dashboard.web.common.IdGenerator;
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

    private static final long twepoch = 1409030641843L;

    private static final long workerIdBits = 9L;

    private static final long dataCenterIdBits = 1L;

    private static final long maxWorkerId = 511L;

    private static final long sequenceBits = 12L;

    private static final long workerIdShift = 12L;

    private static final long dataCenterIdShift = 21L;

    private static final long timestampLeftShift = 22L;

    private static final long sequenceMask = 4095L;

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
        log.warn("Init IdService by dataCenterId:{} and workerId:{}, it maybe duplicate.", Long.valueOf(this.dataCenterId), Long.valueOf(this.workerId));
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
