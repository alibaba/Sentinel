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
package com.alibaba.csp.sentinel.cluster.server;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.alibaba.csp.sentinel.cluster.TokenService;
import com.alibaba.csp.sentinel.cluster.flow.DefaultTokenService;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class TokenServiceProvider {

    private static TokenService service = null;

    private static final ServiceLoader<TokenService> LOADER = ServiceLoader.load(TokenService.class);

    static {
        resolveTokenServiceSpi();
    }

    public static TokenService getService() {
        return service;
    }

    private static void resolveTokenServiceSpi() {
        boolean hasOther = false;
        List<TokenService> list = new ArrayList<TokenService>();
        for (TokenService service : LOADER) {
            if (service.getClass() != DefaultTokenService.class) {
                hasOther = true;
                list.add(service);
            }
        }

        if (hasOther) {
            // Pick the first.
            service = list.get(0);
        } else {
            // No custom token service, using default.
            service = new DefaultTokenService();
        }

        RecordLog.info("[TokenServiceProvider] Global token service resolved: "
            + service.getClass().getCanonicalName());
    }
}
