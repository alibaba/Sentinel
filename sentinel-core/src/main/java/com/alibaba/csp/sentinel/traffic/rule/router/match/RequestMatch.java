/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.traffic.rule.router.match;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.traffic.TrafficContext;

/**
 * @author panxiaojun233
 */
public class RequestMatch {

    private StringMatch serviceName;

    private MethodMatch method;

    private Map<String, StringMatch> attachments = new HashMap<>();

    public StringMatch getServiceName() {
        return serviceName;
    }

    public void setServiceName(StringMatch serviceName) {
        this.serviceName = serviceName;
    }

    public MethodMatch getMethod() {
        return method;
    }

    public void setMethod(MethodMatch method) {
        this.method = method;
    }

    public Map<String, StringMatch> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, StringMatch> attachments) {
        this.attachments = attachments;
    }

    public boolean isMatch(TrafficContext context) {
        // Service name
        if (getServiceName() != null) {
            if (!getServiceName().isMatch(context.getServiceName())) {
                return false;
            }
        }

        if (getMethod() != null) {
            if (!getMethod().isMatch(context)) {
                return false;
            }
        }


        // TODO Match attachments

        return true;
    }
}
