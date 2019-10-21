package com.alibaba.acm.shaded.com.aliyuncs.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CacheTimeHelper {
    
    private static long endPointCacheTime = 3600L; //Seconds
    private static Map<String, Date> lastClearTimeMap = new HashMap<String, Date>();
    
    public static boolean CheckEndPointCacheIsExpire(String product, String regionId)
    {
        String key = product + "_" + regionId;
        Date lastClearTime;
        if (null != lastClearTimeMap.get(key)) {
            lastClearTime = lastClearTimeMap.get(key);
        } else {
            lastClearTime = new Date();
            lastClearTimeMap.put(key, lastClearTime);
        }
        Date currentTime = new Date();
        long elapsedTime = (currentTime.getTime() - lastClearTime.getTime())/1000L;
        if (endPointCacheTime < elapsedTime)
        {
            return true;
        }

        return false;
    }
    
    public static void addLastClearTimePerProduct(String product, String regionId, Date date) {
        String key = product + "_" + regionId;
        lastClearTimeMap.put(key, date);
    }
}
