/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public class EnvoyRlsRule {

    private String domain;
    private List<ResourceDescriptor> descriptors;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<ResourceDescriptor> getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(List<ResourceDescriptor> descriptors) {
        this.descriptors = descriptors;
    }

    @Override
    public String toString() {
        return "EnvoyRlsRule{" +
            "domain='" + domain + '\'' +
            ", descriptors=" + descriptors +
            '}';
    }

    public static class ResourceDescriptor {

        private Set<KeyValueResource> resources;

        private Double count;

        public ResourceDescriptor() {}

        public ResourceDescriptor(Set<KeyValueResource> resources, Double count) {
            this.resources = resources;
            this.count = count;
        }

        public Set<KeyValueResource> getResources() {
            return resources;
        }

        public void setResources(Set<KeyValueResource> resources) {
            this.resources = resources;
        }

        public Double getCount() {
            return count;
        }

        public void setCount(Double count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "ResourceDescriptor{" +
                "resources=" + resources +
                ", count=" + count +
                '}';
        }
    }

    public static class KeyValueResource {

        private String key;
        private String value;

        public KeyValueResource() {}

        public KeyValueResource(String key, String value) {
            AssertUtil.assertNotBlank(key, "key cannot be blank");
            AssertUtil.assertNotBlank(value, "value cannot be blank");
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            KeyValueResource that = (KeyValueResource)o;
            return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return "KeyValueResource{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
        }
    }
}
