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
package com.alibaba.csp.sentinel.dashboard.repository.project;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Set;

/**
 * cache the operation.
 *
 * @author wxq
 */
public class CachedProjectRepository implements ProjectRepository {

    private static final String CACHE_NAME = "apollo-cache-name";

    private final ProjectRepository projectRepository;

    private final CacheManager cacheManager;

    public CachedProjectRepository(ProjectRepository projectRepository, CacheManager cacheManager) {
        this.projectRepository = projectRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public void add(String projectName) {
        this.add(projectName);
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAME)
    public int delete(String projectName) {
        return this.projectRepository.delete(projectName);
    }

    @Override
    @Cacheable(CACHE_NAME)
    public boolean exists(String projectName) {
        return this.projectRepository.exists(projectName);
    }

    @Override
    public Set<String> findAll() {
        Set<String> projectNames = this.projectRepository.findAll();

        // add them to cache
        Cache cache = this.cacheManager.getCache(CACHE_NAME);
        return projectNames;
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public Set<String> deleteAll() {
        return this.deleteAll();
    }

}
