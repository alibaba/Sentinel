package com.alibaba.edas.acm.listener;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import com.taobao.diamond.utils.StringUtils;

public abstract class PropertiesListener extends ConfigChangeListener {

    public void receiveConfigInfo(String configInfo) {
        if (StringUtils.isEmpty(configInfo)) {
            return;
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(configInfo));
            innerReceive(properties);
        }
        catch (IOException e) {
            log.error("DIAMOND-XXXX","load properties error：" + configInfo, e);
        }

    }


    public abstract void innerReceive(Properties properties);

}
