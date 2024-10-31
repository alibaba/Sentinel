/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource.xds.client.cert.manager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.expection.CertificateException;
import com.alibaba.csp.sentinel.datasource.xds.property.repository.CertPairRepository;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.cert.CertPair;

/**
 * Certificate management class,
 * used to get certificates and certificates rotation
 *
 * @author lwj
 * @since 2.0.0
 */
public abstract class AbstractCertManager implements AutoCloseable {

    protected final ScheduledExecutorService schedule;
    protected XdsConfigProperties xdsConfigProperties;
    protected CertPair certPair = null;
    private CertPairRepository certPairRepository;

    public AbstractCertManager(XdsConfigProperties xdsConfigProperties, CertPairRepository certPairRepository) {
        this.xdsConfigProperties = xdsConfigProperties;
        this.certPairRepository = certPairRepository;
        schedule = new ScheduledThreadPoolExecutor(1);
        try {
            getNewCertPair();
        } catch (Exception e) {
            RecordLog.error("[XdsDataSource] Get cert failed.", e);
        }
    }

    /**
     * Get a certificate that is automatically renewed when it expires
     *
     * @return
     */
    public CertPair getCertPair() {
        if (null == certPair) {
            throw new CertificateException("Without any certificate");
        }
        if (System.currentTimeMillis() < certPair.getExpireTime()) {
            return certPair;
        }
        synchronized (this) {
            if (System.currentTimeMillis() < certPair.getExpireTime()) {
                return certPair;
            }
            getNewCertPair();
            return certPair;
        }
    }

    /**
     * Obtain a new certificate and enable the task of obtaining a new certificate after certValidityTimeS *
     * certPeriodRatio
     */
    private synchronized void getNewCertPair() {
        CertPair newCertPair = doGetNewCertPair();
        certPair = newCertPair;

        if (newCertPair != null && newCertPair.getExpireTime() != 0) {
            certPairRepository.update(certPair);
            RecordLog.info("[XdsDataSource] Get new cert = {} ", certPair.toString());
            /**
             * the certificate is requested again after certValidityTimeS * certPeriodRatio
             */
            schedule.schedule(() -> {
                    try {
                        getNewCertPair();
                    } catch (Exception e) {
                        RecordLog.error("[XdsDataSource] Get cert failed.", e);
                    }
                }, (long) ((certPair.getExpireTime() - System.currentTimeMillis())
                    * xdsConfigProperties.getCertPeriodRatio()),
                TimeUnit.MILLISECONDS);
        }
        //If the certificate is null, the request continues
        if (newCertPair == null) {
            schedule.schedule(() -> {
                try {
                    getNewCertPair();
                } catch (Exception e) {
                    RecordLog.error("[XdsDataSource] Get cert failed.", e);
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Get a new certificate
     *
     * @return
     */
    protected abstract CertPair doGetNewCertPair();

    public void close() {
        schedule.shutdown();
    }

}
