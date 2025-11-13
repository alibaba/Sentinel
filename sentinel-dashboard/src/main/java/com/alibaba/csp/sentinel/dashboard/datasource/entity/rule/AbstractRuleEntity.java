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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import java.util.Date;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.Rule;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
public abstract class AbstractRuleEntity<T extends AbstractRule> implements RuleEntity {

    protected Long id;

    protected String app;
    protected String ip;
    protected Integer port;

    protected T rule;

    /**
     * Whether to match resource names according to regular rules
     */
    private boolean regex = false;

    private Date gmtCreate;
    private Date gmtModified;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getApp() {
        return app;
    }

    public AbstractRuleEntity<T> setApp(String app) {
        this.app = app;
        return this;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public AbstractRuleEntity<T> setIp(String ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public AbstractRuleEntity<T> setPort(Integer port) {
        this.port = port;
        return this;
    }

    public T getRule() {
        return rule;
    }

    public AbstractRuleEntity<T> setRule(T rule) {
        this.rule = rule;
        return this;
    }

    public boolean isRegex() {
        return regex;
    }

    public AbstractRuleEntity<T> setRegex(boolean regex) {
        this.regex = regex;
        return this;
    }

    @Override
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public AbstractRuleEntity<T> setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public AbstractRuleEntity<T> setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    @Override
    public T toRule() {
        return rule;
    }
}
