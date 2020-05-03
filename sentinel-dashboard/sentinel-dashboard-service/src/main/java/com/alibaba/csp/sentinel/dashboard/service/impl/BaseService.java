/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.service.impl;

import com.alibaba.csp.sentinel.dashboard.service.api.exception.DashboardServiceException;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author cdfive
 */
public class BaseService {

    protected void checkCondition(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    protected void checkRange(Integer fieldValue, Integer min, Integer max, String fieldName) {
        if (fieldValue < min || fieldValue > max) {
            fail(fieldName + " must be " + min + "-" + max + ", but " + fieldValue + " got");
        }
    }

    protected void checkInValues(Integer fieldValue, String fieldName, Integer... values) {
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

    protected void checkNotNull(Object fieldValue, String fieldName) {
        if (fieldValue == null) {
            fail(fieldName + " can't be null");
        }
    }

    protected void checkNotNullMessage(Object fieldValue, String message) {
        if (fieldValue == null) {
            fail(message);
        }
    }

    protected void checkNotBlank(String fieldValue, String fieldName) {
        if (StringUtil.isBlank(fieldValue)) {
            fail(fieldName + " can't be null or empty");
        }
    }

    protected void checkNotBlankMessage(String fieldValue, String message) {
        if (StringUtil.isBlank(fieldValue)) {
            fail(message);
        }
    }

    protected void fail(String message) {
        throw new DashboardServiceException(message);
    }

    protected void fail(Throwable ex) {
        throw new DashboardServiceException(ex);
    }

    protected void fail(String message, Throwable ex) {
        throw new DashboardServiceException(ex);
    }

    protected void fail() {
        throw new DashboardServiceException();
    }
}
