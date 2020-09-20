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

import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;

import java.util.Arrays;
import java.util.List;

/**
 * @author sea
 */
class SentinelNativeImageProcessor {

    private static final String FEATURE_NATIVE_IMAGE = "sentinel-native-image";

    @BuildStep
    void feature(BuildProducer<FeatureBuildItem> featureProducer) {
        featureProducer.produce(new FeatureBuildItem(FEATURE_NATIVE_IMAGE));
    }

    @BuildStep(onlyIf = NativeBuild.class)
    List<RuntimeInitializedClassBuildItem> runtimeInitializedClasses() {
        return Arrays.asList(
                new RuntimeInitializedClassBuildItem("com.alibaba.fastjson.serializer.JodaCodec"),
                new RuntimeInitializedClassBuildItem("com.alibaba.fastjson.serializer.GuavaCodec"),
                new RuntimeInitializedClassBuildItem("com.alibaba.fastjson.support.moneta.MonetaCodec"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.Env"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.init.InitExecutor"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.cluster.ClusterStateManager"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.node.metric.MetricTimerListener"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.node.metric.MetricWriter"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.util.TimeUtil"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.eagleeye.StatLogController"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.slots.logger.EagleEyeLogUtil"),
                new RuntimeInitializedClassBuildItem("com.alibaba.csp.sentinel.eagleeye.EagleEye"));
    }

    @BuildStep(onlyIf = NativeBuild.class)
    ReflectiveClassBuildItem setupSentinelReflectiveClasses() {
        return new ReflectiveClassBuildItem(true, true, true,
                DefaultSlotChainBuilder.class.getName());
    }

    @BuildStep(onlyIf = NativeBuild.class)
    @Record(ExecutionTime.STATIC_INIT)
    void record(SentinelRecorder recorder) {
        recorder.init();
    }
}
