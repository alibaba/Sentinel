package com.alibaba.csp.ahas.sentinel;

import com.alibaba.csp.sentinel.datasource.acm.Decryptor;
import com.alibaba.csp.sentinel.log.RecordLog;

import com.taobao.csp.ahas.auth.api.AuthUtil;

/**
 * @author leyou 2019/3/11
 */
class SimpleDecryptor implements Decryptor {

    @Override
    public String decrypt(String source) throws Exception {
        try {
            return AuthUtil.decrypt(AuthUtil.getSecretKey(), source);
        } catch (Exception e) {
            // decrypt fail, try return original source
            RecordLog.info("decrypt fail, will use source as it is", e);
            return source;
        }
    }
}
