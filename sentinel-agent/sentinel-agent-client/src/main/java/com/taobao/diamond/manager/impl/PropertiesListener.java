package com.taobao.diamond.manager.impl;

import com.taobao.diamond.manager.ManagerListenerAdapter;
import com.taobao.diamond.utils.StringUtils;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import static com.taobao.diamond.client.impl.DiamondEnv.log;



public abstract class PropertiesListener extends ManagerListenerAdapter {

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
            log.error("DIAMOND-XXXX","load properties error��" + configInfo, e);
        }

    }


    public abstract void innerReceive(Properties properties);

}
