/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.trust;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.trust.auth.Rules;
import com.alibaba.csp.sentinel.trust.cert.CertPair;
import com.alibaba.csp.sentinel.trust.tls.TlsMode;

/**
 * Manager of Sentinel zero-trust cert and rules.
 *
 * @author lwj
 * @since 2.0.0
 */
public class TrustManager {

    private static volatile TrustManager instance = null;

    private CertPair certPair = null;
    private List<StoreCallback<CertPair>> certStoreCallbackList = new ArrayList<>();
    private TlsMode tlsMode = null;
    private List<StoreCallback<TlsMode>> tlsModeStoreCallbackList = new ArrayList<>();
    private Rules rules = null;
    private List<StoreCallback<Rules>> rulesStoreCallbackList = new ArrayList<>();

    public static TrustManager getInstance() {
        if (null != instance) {
            return instance;
        }
        synchronized (TrustManager.class) {
            if (null != instance) {
                return instance;
            }
            instance = new TrustManager();
            return instance;
        }
    }

    public synchronized void storeCertPair(CertPair certPair) {
        this.certPair = certPair;
        certStoreCallbackList.forEach(c -> c.onUpdate(certPair));
    }

    public synchronized void storeTlsMode(TlsMode tlsMode) {
        this.tlsMode = tlsMode;
        tlsModeStoreCallbackList.forEach(c -> c.onUpdate(tlsMode));
    }

    public synchronized void storeRules(Rules rules) {
        this.rules = rules;
        rulesStoreCallbackList.forEach(c -> c.onUpdate(rules));
    }

    public void registerCertCallback(StoreCallback<CertPair> callback) {
        certStoreCallbackList.add(callback);
    }

    public void registerTlsModeCallback(StoreCallback<TlsMode> callback) {
        tlsModeStoreCallbackList.add(callback);
    }

    public void registerRulesCallback(StoreCallback<Rules> callback) {
        rulesStoreCallbackList.add(callback);
    }

    public void removeAllCertCallback() {
        certStoreCallbackList.clear();
    }

    public void removeAllTlsModeCallback() {
        tlsModeStoreCallbackList.clear();
    }

    public void removeAllRulesCallback() {
        rulesStoreCallbackList.clear();
    }

    public CertPair getCertPair() {
        return certPair;
    }

    public TlsMode getTlsMode() {
        return tlsMode;
    }

    public Rules getRules() {
        return rules;
    }

}
