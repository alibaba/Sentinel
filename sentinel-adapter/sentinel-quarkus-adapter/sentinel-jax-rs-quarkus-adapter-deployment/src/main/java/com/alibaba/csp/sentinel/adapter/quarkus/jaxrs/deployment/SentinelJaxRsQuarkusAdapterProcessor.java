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
package com.alibaba.csp.sentinel.adapter.quarkus.jaxrs.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.jboss.logging.Logger;

/**
 * @author sea
 */
class SentinelJaxRsQuarkusAdapterProcessor {

    private static final Logger logger = Logger.getLogger(SentinelJaxRsQuarkusAdapterProcessor.class);

    private static final String FEATURE_JAX_RS = "sentinel-jax-rs";

    @BuildStep
    void feature(BuildProducer<FeatureBuildItem> featureProducer) {
        featureProducer.produce(new FeatureBuildItem(FEATURE_JAX_RS));
    }

}
