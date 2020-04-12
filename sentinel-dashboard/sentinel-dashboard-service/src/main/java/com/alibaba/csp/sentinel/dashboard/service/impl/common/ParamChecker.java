package com.alibaba.csp.sentinel.dashboard.service.impl.common;

import com.alibaba.csp.sentinel.dashboard.service.api.exception.DashboardServiceException;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author cdfive
 */
public class ParamChecker {
    
    public static void checkCondition(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    public static void checkRange(Integer fieldValue, Integer min, Integer max, String fieldName) {
        if (fieldValue < min || fieldValue > max) {
            fail(fieldName + " must be " + min + "-" + max + ", but " + fieldValue + " got");
        }
    }

    public static void checkInValues(Integer fieldValue, String fieldName, Integer... values) {
        for (Integer value : values) {
            if (value.equals(fieldValue)) {
                return;
            }
        }

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (!first) {
                if (i != values.length - 1) {
                    sb.append(", ");
                } else {
                    sb.append(" or ");
                }
            }
            sb.append(values[i]);
        }
        fail(fieldName + " must be " + sb.toString() + ", but " + fieldValue + " got");
    }

    public static void checkNotNull(Object fieldValue, String fieldName) {
        if (fieldValue == null) {
            fail(fieldName + " can't be null");
        }
    }

    public static void checkNotNullMessage(Object fieldValue, String message) {
        if (fieldValue == null) {
            fail(message);
        }
    }

    public static void checkNotBlank(String fieldValue, String fieldName) {
        if (StringUtil.isBlank(fieldValue)) {
            fail(fieldName + " can't be null or empty");
        }
    }

    public static void checkNotBlankMessage(String fieldValue, String message) {
        if (StringUtil.isBlank(fieldValue)) {
            fail(message);
        }
    }

    public static void fail(String message) {
        throw new DashboardServiceException(message);
    }

    public static void fail(Throwable ex) {
        throw new DashboardServiceException(ex);
    }

    public static void fail(String message, Throwable ex) {
        throw new DashboardServiceException(ex);
    }

    public static void fail() {
        throw new DashboardServiceException();
    }
}
