package com.alibaba.csp.sentinel.slots.automatic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutomaticStatistics {

    /**
     * 监控数据
     */
    private static Map<String, Integer> totalQps ;

    private static Map<String, Integer> passedQps ;

    private static Map<String, Double> minRt;

    private static Map<String, Double> avgRt;

    static {
        totalQps = new ConcurrentHashMap<String, Integer>();
        passedQps = new ConcurrentHashMap<String, Integer>();
        minRt = new ConcurrentHashMap<String, Double>();
        avgRt = new ConcurrentHashMap<String, Double>();
    }

    static public void setTotalQps(String resource, Integer qps){
        totalQps.put(resource,qps);
    }

    static public void setPassedQps(String resource, Integer qps){
        passedQps.put(resource,qps);
    }

    static public void setMinRt(String resource, Double rt){
        minRt.put(resource,rt);
    }

    static public void setAvgRt(String resource, Double rt){
        avgRt.put(resource,rt);
    }

    static public Integer getTotalQps(String resource){
        if (totalQps.get(resource) == null) {
            return 0;
        } else {
            return totalQps.get(resource);
        }
    }

    static public int getPassedQps(String resource){
        if (passedQps.get(resource) == null) {
            return 0;
        } else {
            return passedQps.get(resource);
        }
    }

    static public double getMinRt(String resource){
        if (minRt.get(resource) == null||minRt.get(resource)<=0) {
            return 1;
        } else {
            return minRt.get(resource);
        }
    }


    static public double getAvgRt(String resource){
        if (avgRt.get(resource) == null||minRt.get(resource)<=0) {
            return 1;
        } else {
            return avgRt.get(resource);
        }
    }
}
