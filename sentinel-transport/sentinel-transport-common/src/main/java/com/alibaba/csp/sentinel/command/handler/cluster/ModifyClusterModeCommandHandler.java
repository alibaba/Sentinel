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

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@CommandMapping(name = "setClusterMode")
public class ModifyClusterModeCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        try {
            int mode = Integer.valueOf(request.getParam("mode"));
            RecordLog.info("[ModifyClusterModeCommandHandler] Modifying cluster mode to: " + mode);
            if (ClusterStateManager.applyState(mode)) {
                return CommandResponse.ofSuccess("success");
            } else {
                return CommandResponse.ofSuccess("failed");
            }
        } catch (NumberFormatException ex) {
            return CommandResponse.ofFailure(new IllegalArgumentException("invalid mode"));
        } catch (Exception ex) {
            return CommandResponse.ofFailure(ex);
        }
    }
}
