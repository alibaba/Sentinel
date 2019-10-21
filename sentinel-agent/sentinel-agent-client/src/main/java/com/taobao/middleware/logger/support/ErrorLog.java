/*
 * Copyright 2014 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.taobao.middleware.logger.support;

/**
 * 兼容老的ErrorLog，后续请使用{@link LoggerHelper}
 * 
 * @author zhuyong 2014年7月1日 上午11:41:22
 */
public class ErrorLog {

    public static String buildErrorMsg(String msg, String errorCode, String errorType) {
        return LoggerHelper.getErrorCodeStr(null, errorCode, errorType, msg);
    }
}
