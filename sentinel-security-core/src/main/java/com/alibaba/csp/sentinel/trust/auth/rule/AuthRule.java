/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.trust.auth.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alibaba.csp.sentinel.trust.auth.condition.AuthCondition;
import com.alibaba.csp.sentinel.util.CollectionUtil;

/**
 * @author lwj
 * @since 2.0.0
 */
public class AuthRule {

    /**
     * Child rule chaining Type
     */
    private final ChildChainType childChainType;
    private final List<AuthRule> children;
    private final AuthCondition condition;
    private final boolean not;

    public AuthRule(ChildChainType childChainType) {
        this.childChainType = childChainType;
        this.children = new ArrayList<>();
        this.condition = null;
        this.not = false;

    }

    public AuthRule(ChildChainType childChainType, boolean not) {
        this.childChainType = childChainType;
        this.children = new ArrayList<>();
        this.condition = null;
        this.not = not;
    }

    public AuthRule(AuthCondition condition) {
        this.childChainType = ChildChainType.LEAF;
        this.children = null;
        this.condition = condition;
        this.not = false;
    }

    public void addChildren(AuthRule rule) {
        children.add(rule);
    }

    public boolean isEmpty() {
        if (isLeaf()) {
            return true;
        }
        return CollectionUtil.isEmpty(children);
    }

    public boolean isLeaf() {
        return childChainType == ChildChainType.LEAF;
    }

    public ChildChainType getChildChainType() {
        return childChainType;
    }

    public List<AuthRule> getChildren() {
        return children;
    }

    public AuthCondition getCondition() {
        return condition;
    }

    public boolean isNot() {
        return not;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthRule authRule = (AuthRule) o;
        return not == authRule.not && childChainType == authRule.childChainType && Objects.equals(children,
            authRule.children) && Objects.equals(condition, authRule.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childChainType, children, condition, not);
    }

    @Override
    public String toString() {
        return "AuthRule{" +
            "op=" + childChainType +
            ", children=" + children +
            ", condition=" + condition +
            ", isNot=" + not +
            '}';
    }

    /**
     * Child rule chaining
     */
    public enum ChildChainType {
        /**
         * It's already a leaf node
         */
        LEAF,
        AND,
        OR,
        ;

    }
}
