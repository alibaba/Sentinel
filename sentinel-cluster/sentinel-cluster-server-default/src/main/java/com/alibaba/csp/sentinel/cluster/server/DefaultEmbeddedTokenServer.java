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

import java.util.Collection;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenService;

/**
 * Default embedded token server in Sentinel which wraps the {@link SentinelDefaultTokenServer}
 * and the {@link TokenService} from SPI provider.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class DefaultEmbeddedTokenServer implements EmbeddedClusterTokenServer {

    private final TokenService tokenService = TokenServiceProvider.getService();
    private final ClusterTokenServer server = new SentinelDefaultTokenServer(true);

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }

    @Override
    public TokenResult requestToken(Long ruleId, int acquireCount, boolean prioritized) {
        if (tokenService != null) {
            return tokenService.requestToken(ruleId, acquireCount, prioritized);
        }
        return new TokenResult(TokenResultStatus.FAIL);
    }

    @Override
    public TokenResult requestParamToken(Long ruleId, int acquireCount, Collection<Object> params) {
        if (tokenService != null) {
            return tokenService.requestParamToken(ruleId, acquireCount, params);
        }
        return new TokenResult(TokenResultStatus.FAIL);
    }
}
