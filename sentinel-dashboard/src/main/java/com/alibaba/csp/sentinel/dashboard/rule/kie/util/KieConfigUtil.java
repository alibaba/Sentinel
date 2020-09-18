package com.alibaba.csp.sentinel.dashboard.rule.kie.util;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigItem;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigLabels;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KieConfigUtil {

    /**
     * Judge target item by conditions.
     *
     * @param item config item
     * @return judge result
     */
    public static boolean isTargetItem(String key, KieServerInfo serverInfo, KieConfigItem item){
        KieConfigLabels labels = item.getLabels();

        return key.equals(item.getKey())
                && serverInfo.getApp().equals(labels.getApp())
                && serverInfo.getService().equals(labels.getService())
                && serverInfo.getServerVersion().equals(labels.getVersion())
                && serverInfo.getEnvironment().equals(labels.getEnvironment());
    }

    public static <T> List<T> parseKieConfig(Class<T> ruleClass, KieServerInfo kieServerInfo,
                                             KieConfigItem item){
        if (Objects.isNull(item) || StringUtils.isEmpty(item.getValue()) || Objects.isNull(kieServerInfo)){
            return Collections.emptyList();
        }

        return JSON.parseObject(item.getValue(), new TypeReference<List<T>>(ruleClass) {});
    }
}
