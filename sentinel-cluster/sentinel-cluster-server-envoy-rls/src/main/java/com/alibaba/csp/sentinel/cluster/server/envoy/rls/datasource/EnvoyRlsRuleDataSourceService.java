/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls.datasource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.alibaba.csp.sentinel.cluster.server.envoy.rls.SentinelEnvoyRlsConstants;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule.EnvoyRlsRule;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule.EnvoyRlsRuleManager;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public class EnvoyRlsRuleDataSourceService {

    private final Yaml yaml;
    private ReadableDataSource<String, List<EnvoyRlsRule>> ds;

    public EnvoyRlsRuleDataSourceService() {
        this.yaml = createYamlParser();
    }

    private Yaml createYamlParser() {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        return new Yaml(representer);
    }

    public synchronized void init() throws Exception {
        if (ds != null) {
            return;
        }
        String configPath = getRuleConfigPath();
        if (StringUtil.isBlank(configPath)) {
            throw new IllegalStateException("Empty rule config path, please set the file path in the env: "
                + SentinelEnvoyRlsConstants.RULE_FILE_PATH_ENV_KEY);
        }

        this.ds = new FileRefreshableDataSource<>(configPath, s -> Arrays.asList(yaml.loadAs(s, EnvoyRlsRule.class)));
        EnvoyRlsRuleManager.register2Property(ds.getProperty());
    }

    public synchronized void onShutdown() {
        if (ds != null) {
            try {
                ds.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getRuleConfigPath() {
        return Optional.ofNullable(System.getenv(SentinelEnvoyRlsConstants.RULE_FILE_PATH_ENV_KEY))
            .orElse(SentinelConfig.getConfig(SentinelEnvoyRlsConstants.RULE_FILE_PATH_PROPERTY_KEY));
    }
}
