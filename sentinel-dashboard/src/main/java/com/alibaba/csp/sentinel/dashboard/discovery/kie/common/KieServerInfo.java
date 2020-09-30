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
package com.alibaba.csp.sentinel.dashboard.discovery.kie.common;

import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KieServerInfo extends AppInfo {
    private String id;

    private KieServerLabel label;

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KieServerInfo)) {
            return false;
        }
        KieServerInfo that = (KieServerInfo) o;
        return label.equals(that.label);
    }

    @Override
    public String toString() {
        return new StringBuilder("KieServerInfo {")
                .append("label='").append(label).append('\'')
                .append(", id='").append(id).append('\'')
                .append('}').toString();
    }
}
