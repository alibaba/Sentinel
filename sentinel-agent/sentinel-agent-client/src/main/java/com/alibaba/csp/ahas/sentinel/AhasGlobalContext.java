package com.alibaba.csp.ahas.sentinel;

import com.taobao.csp.ahas.service.api.client.ClientInfoService;

/**
 * @author Daniel Tsai
 */
public class AhasGlobalContext {

    private static ClientInfoService clientInfoService;

    public static ClientInfoService getClientInfoService() {
        return clientInfoService;
    }

    public static void setClientInfoService(ClientInfoService clientInfoService) {
        AhasGlobalContext.clientInfoService = clientInfoService;
    }
}
