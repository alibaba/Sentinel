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
package com.alibaba.csp.sentinel.dashboard.repository.rule;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;

/**
 * Interface to store and find rules.
 *
 * @author leyou
 */
public interface RuleRepository<T, ID> {

    /**
     * Save one.
     *
     * @param entity
     * @return
     */
    T save(T entity);

    /**
     * Save all.
     *
     * @param rules
     * @return rules saved.
     */
    List<T> saveAll(List<T> rules);

    /**
     * Delete by id
     *
     * @param id
     * @return entity deleted
     */
    T delete(ID id);

    /**
     * Find by id.
     *
     * @param id
     * @return
     */
    T findById(ID id);

    /**
     * Find all by machine.
     *
     * @param machineInfo
     * @return
     */
    List<T> findAllByMachine(MachineInfo machineInfo);

    /**
     * Find all by application.
     *
     * @param appName valid app name
     * @return all rules of the application
     * @since 1.4.0
     */
    List<T> findAllByApp(String appName);

    ///**
    // * Find all by app and enable switch.
    // * @param app
    // * @param enable
    // * @return
    // */
    //List<T> findAllByAppAndEnable(String app, boolean enable);
}
