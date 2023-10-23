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
package com.alibaba.csp.sentinel.datasource.xds.property;

import com.alibaba.csp.sentinel.datasource.xds.property.repository.AuthRepository;
import com.alibaba.csp.sentinel.datasource.xds.property.repository.CertPairRepository;
import com.alibaba.csp.sentinel.datasource.xds.property.repository.TlsModeRepository;

/**
 * @author lwj
 * @since 2.0.0
 */
public class XdsProperty {
    private final AuthRepository authRepository = new AuthRepository();
    private final TlsModeRepository tlsModeRepository = new TlsModeRepository();
    private final CertPairRepository certPairRepository = new CertPairRepository();

    public XdsProperty() {

    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public TlsModeRepository getTlsModeRepository() {
        return tlsModeRepository;
    }

    public CertPairRepository getCertPairRepository() {
        return certPairRepository;
    }
}
