package com.alibaba.csp.sentinel.dashboard.rule.kie.util;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigItem;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigLabels;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KieConfigUtil {

    /**
     * Judge target item by conditions.
     *
     * @param item config item
     * @return judge result
     */
    private static boolean isTargetItem(String key, KieServerInfo serverInfo, KieConfigItem item){
        KieConfigLabels labels = item.getLabels();

        return key.equals(item.getKey())
                && serverInfo.getApp().equals(labels.getApp())
                && serverInfo.getService().equals(labels.getService())
                && serverInfo.getServerVersion().equals(labels.getVersion())
                && serverInfo.getEnvironment().equals(labels.getEnvironment());
    }

    public static <T> List<T> parseKieConfig(Class<T> ruleClass, KieServerInfo kieServerInfo,
                                                  KieConfigResponse config){
        if (Objects.isNull(config) || Objects.isNull(kieServerInfo)){
            return Collections.emptyList();
        }

        String ruleKey = ruleClass.getSimpleName();

        Optional<String> ruleString = config.getData().stream()
                .filter(item -> isTargetItem(ruleKey, kieServerInfo, item))
                .map(KieConfigItem::getValue)
                .findFirst();

        if(!ruleString.isPresent()){
            return Collections.emptyList();
        }

        return JSON.parseObject(ruleString.get(), new TypeReference<List<T>>(ruleClass) {});
    }
}
