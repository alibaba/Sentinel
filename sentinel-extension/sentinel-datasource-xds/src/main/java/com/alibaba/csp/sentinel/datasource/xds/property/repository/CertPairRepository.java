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
package com.alibaba.csp.sentinel.datasource.xds.property.repository;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.trust.cert.CertPair;

/**
 * @author lwj
 * @since 2.0.0
 */
public class CertPairRepository extends Repository<CertPair> {

    @Override
    protected void updateEnd(CertPair newInstance) {
        RecordLog.info("[XdsDataSource] Update new CertPair={}", newInstance);
    }
}
