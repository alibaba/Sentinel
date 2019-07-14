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
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态规则拉取zk实现方式基类
 * @param <T> 需传入FlowRuleEntity、SystemRuleEntity、或者DegradeRuleEntity
 * @author lixiangqian
 */
public abstract class  AbstractRuleZookeeperProvider<T> implements DynamicRuleProvider<List<T>> {
    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    private ZookeeperConfig zookeeperConfig;
    protected abstract String getType();
    protected abstract  Converter<String, List<T>> getConverter();

    @Override
    public List<T> getRules(String appName) throws Exception {
        String zkPath = zookeeperConfig.getPath(appName, getType());
        byte[] bytes = zkClient.getData().forPath(zkPath);
        if (null == bytes || bytes.length == 0) {
            return new ArrayList<>();
        }
        String s = new String(bytes);

        return getConverter().convert(s);
    }

}