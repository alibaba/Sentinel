package com.alibaba.jvm.sandbox.qatest.api.util;

public class ApiQaArrayUtils {

    public static <T> boolean has(T target, T... targetArray) {
        for(T t : targetArray) {
            if(target.equals(t)) {
                return true;
            }
        }
        return false;
    }

}
