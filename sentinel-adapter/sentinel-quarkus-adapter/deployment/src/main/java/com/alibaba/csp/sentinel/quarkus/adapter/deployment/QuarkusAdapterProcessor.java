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
package com.alibaba.csp.sentinel.quarkus.adapter.deployment;

import com.alibaba.csp.sentinel.adapter.jaxrs.SentinelJaxRsProviderFilter;
import com.alibaba.csp.sentinel.adapter.jaxrs.exception.DefaultExceptionMapper;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;

class QuarkusAdapterProcessor {

    private static final String FEATURE = "sentinel-quarkus-adapter";

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    void feature(BuildProducer<FeatureBuildItem> featureProducer) {
        featureProducer.produce(new FeatureBuildItem(FEATURE));
    }

    @BuildStep
    void setUpSecurity(BuildProducer<ResteasyJaxrsProviderBuildItem> providers) {
        providers.produce(new ResteasyJaxrsProviderBuildItem(DefaultExceptionMapper.class.getName()));
        providers.produce(new ResteasyJaxrsProviderBuildItem(SentinelJaxRsProviderFilter.class.getName()));
    }

}
