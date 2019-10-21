package com.alibaba.csp.ahas.sentinel.adapter;

import com.alibaba.csp.ahas.sentinel.adapter.servlet.PropertyUrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.DefaultUrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author Eric Zhao
 */
public class AhasAdapterExtInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        UrlCleaner oldCleaner = WebCallbackManager.getUrlCleaner();
        if (oldCleaner != null && !(oldCleaner instanceof DefaultUrlCleaner)) {
            RecordLog.info("[AhasAdapterExtInitFunc] Customized UrlCleaner has been provided, so "
                + "the AhasPropertyUrlCleaner won't be registered");
        } else {
            WebCallbackManager.setUrlCleaner(new PropertyUrlCleaner());
            RecordLog.info("[AhasAdapterExtInitFunc] Registering {0} to WebCallbackManager",
                PropertyUrlCleaner.class.getSimpleName());
        }
    }
}
