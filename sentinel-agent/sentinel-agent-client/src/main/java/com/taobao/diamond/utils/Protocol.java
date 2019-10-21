package com.taobao.diamond.utils;

/**
 * 用来处理协议相关的操作
 * 
 * @author zhidao
 * @version 1.0 2011/05/03
 * 
 */
public class Protocol {
    /**
     * 解析类于2.0.4(major.minor.bug-fix这样的版本为数字)
     * 
     * @param version
     * @return
     */
    public static int getVersionNumber(String version) {
        if (version == null)
            return -1;

        String[] vs = version.split("\\.");
        int sum = 0;
        for (int i = 0; i < vs.length; i++) {
            try {
                sum = sum * 10 + Integer.parseInt(vs[i]);
            }
            catch (Exception e) {
                // ignore
            }
        }
        return sum;
    }
}
