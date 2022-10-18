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
package com.alibaba.csp.sentinel.adapter.dubbo3;

import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.cluster.filter.ClusterFilter;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.apache.dubbo.rpc.model.ModuleModel;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class SentinelFilterTest {
    @Test
    public void test() {
        ApplicationModel applicationModel = FrameworkModel.defaultModel().newApplication();
        ModuleModel moduleModel = applicationModel.newModule();
        Set<Filter> filters = moduleModel.getExtensionLoader(Filter.class).getSupportedExtensionInstances();
        Assert.assertTrue(filters.stream().anyMatch(f -> f instanceof SentinelDubboProviderFilter));

        Set<ClusterFilter> clusterFilters = moduleModel.getExtensionLoader(ClusterFilter.class).getSupportedExtensionInstances();
        Assert.assertTrue(clusterFilters.stream().anyMatch(f -> f instanceof DubboAppContextFilter));
        Assert.assertTrue(clusterFilters.stream().anyMatch(f -> f instanceof SentinelDubboConsumerFilter));

        applicationModel.destroy();
    }
}
