/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.quarkus.nativeimage;

import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import io.quarkus.runtime.annotations.Recorder;

/**
 * @author sea
 */
@Recorder
public class SentinelRecorder {

    /**
     * register fastjson serializer deserializer class info
     */
    public void init() {
        SerializeConfig.getGlobalInstance().getObjectWriter(NodeVo.class);
        SerializeConfig.getGlobalInstance().getObjectWriter(FlowRule.class);
        SerializeConfig.getGlobalInstance().getObjectWriter(SystemRule.class);
        SerializeConfig.getGlobalInstance().getObjectWriter(DegradeRule.class);
        SerializeConfig.getGlobalInstance().getObjectWriter(AuthorityRule.class);
        SerializeConfig.getGlobalInstance().getObjectWriter(ParamFlowRule.class);

        ParserConfig.getGlobalInstance().getDeserializer(NodeVo.class);
        ParserConfig.getGlobalInstance().getDeserializer(FlowRule.class);
        ParserConfig.getGlobalInstance().getDeserializer(SystemRule.class);
        ParserConfig.getGlobalInstance().getDeserializer(DegradeRule.class);
        ParserConfig.getGlobalInstance().getDeserializer(AuthorityRule.class);
        ParserConfig.getGlobalInstance().getDeserializer(ParamFlowRule.class);
    }
}
