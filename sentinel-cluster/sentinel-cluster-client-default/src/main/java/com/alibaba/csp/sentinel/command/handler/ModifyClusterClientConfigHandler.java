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

import java.net.URLDecoder;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.command.CommandConstants;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.command.entity.ClusterClientStateEntity;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@CommandMapping(name = "cluster/client/modifyConfig", desc = "modify cluster client config")
public class ModifyClusterClientConfigHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String data = request.getParam("data");
        if (StringUtil.isBlank(data)) {
            return CommandResponse.ofFailure(new IllegalArgumentException("empty data"));
        }
        try {
            data = URLDecoder.decode(data, "utf-8");
            RecordLog.info("[ModifyClusterClientConfigHandler] Receiving cluster client config: " + data);
            ClusterClientStateEntity entity = JSON.parseObject(data, ClusterClientStateEntity.class);

            ClusterClientConfigManager.applyNewConfig(entity.toClientConfig());
            ClusterClientConfigManager.applyNewAssignConfig(entity.toAssignConfig());

            return CommandResponse.ofSuccess(CommandConstants.MSG_SUCCESS);
        } catch (Exception e) {
            RecordLog.warn("[ModifyClusterClientConfigHandler] Decode client cluster config error", e);
            return CommandResponse.ofFailure(e, "decode client cluster config error");
        }
    }
}

