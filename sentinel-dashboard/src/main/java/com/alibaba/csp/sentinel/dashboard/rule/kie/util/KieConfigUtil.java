package com.alibaba.csp.sentinel.dashboard.rule.kie.util;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigItem;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigLabels;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerInfo;
import com.alibaba.fastjson.JSON;

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

    public static <T> T parseKieConfig(Class<T> ruleClass, KieConfigItem item){
        return JSON.parseObject(item.getValue(), ruleClass);
    }
}
