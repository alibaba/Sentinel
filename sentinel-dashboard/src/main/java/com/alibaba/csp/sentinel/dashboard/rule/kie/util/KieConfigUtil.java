package com.alibaba.csp.sentinel.dashboard.rule.kie.util;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigItem;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigLabel;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public class KieConfigUtil {

    /**
     * Judge target item by conditions.
     *
     * @param item config item
     * @return judge result
     */
    public static boolean isTargetItem(String key, KieServerInfo serverInfo, KieConfigItem item){
        if(Objects.isNull(item) || Objects.isNull(item.getLabels()) || StringUtils.isEmpty(key) ||
                Objects.isNull(serverInfo)){
            return false;
        }

        KieConfigLabel configLabel = item.getLabels();
        KieServerLabel serverLabel = serverInfo.getLabel();

        return key.equals(item.getKey())
                && serverLabel.getApp().equals(configLabel.getApp())
                && serverLabel.getService().equals(configLabel.getService())
                && serverLabel.getServerVersion().equals(configLabel.getVersion())
                && serverLabel.getEnvironment().equals(configLabel.getEnvironment());
    }

    public static <T> T parseKieConfig(Class<T> ruleClass, KieConfigItem item){
        return JSON.parseObject(item.getValue(), ruleClass);
    }
}
