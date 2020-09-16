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
package com.alibaba.csp.sentinel.command.handler.cluster;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServerProvider;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.serialization.common.JsonTransformerLoader;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@CommandMapping(name = "getClusterMode", desc = "get cluster mode status")
public class FetchClusterModeCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        Map<String, Object> res = new HashMap<String, Object>(4);
        res.put("mode", ClusterStateManager.getMode());
        res.put("lastModified", ClusterStateManager.getLastModified());
        res.put("clientAvailable", isClusterClientSpiAvailable());
        res.put("serverAvailable", isClusterServerSpiAvailable());
        return CommandResponse.ofSuccess(JsonTransformerLoader.serializer().serialize(res));
    }

    private boolean isClusterClientSpiAvailable() {
        return TokenClientProvider.getClient() != null;
    }

    private boolean isClusterServerSpiAvailable() {
        return EmbeddedClusterTokenServerProvider.getServer() != null;
    }
}
