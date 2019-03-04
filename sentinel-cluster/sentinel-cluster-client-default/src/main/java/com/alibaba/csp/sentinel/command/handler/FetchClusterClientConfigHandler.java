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
package com.alibaba.csp.sentinel.command.handler;

import com.alibaba.csp.sentinel.cluster.client.ClientConstants;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.command.entity.ClusterClientStateEntity;
import com.alibaba.fastjson.JSON;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@CommandMapping(name = "cluster/client/fetchConfig", desc = "get cluster client config")
public class FetchClusterClientConfigHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        ClusterClientStateEntity stateVO = new ClusterClientStateEntity()
            .setServerHost(ClusterClientConfigManager.getServerHost())
            .setServerPort(ClusterClientConfigManager.getServerPort())
            .setRequestTimeout(ClusterClientConfigManager.getRequestTimeout());
        if (TokenClientProvider.isClientSpiAvailable()) {
            stateVO.setClientState(TokenClientProvider.getClient().getState());
        } else {
            stateVO.setClientState(ClientConstants.CLIENT_STATUS_OFF);
        }
        return CommandResponse.ofSuccess(JSON.toJSONString(stateVO));
    }
}

