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
package com.alibaba.csp.sentinel.datasource.xds.property.repository;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author lwj
 * @since 2.0.0
 */
public class RepositoryTest {

    @Test
    public void testGetAndUpdateInstance() {
        MockRepository mockRepository = new MockRepository();
        mockRepository.update(1);
        assertEquals(new Integer(1), mockRepository.getInstance());
    }

    @Test
    public void testRegistryRepositoryUpdateCallback() {
        AtomicInteger callback = new AtomicInteger(0);
        MockRepository mockRepository = new MockRepository();
        mockRepository.registryRepositoryUpdateCallback((c) -> callback.getAndIncrement());
        mockRepository.update(1);
        assertEquals(1, callback.get());
    }

    @Test
    public void testUpdateEnd() {
        MockRepository mockRepository = new MockRepository();
        mockRepository.update(1);
        assertEquals(5, mockRepository.end);
    }

    public static class MockRepository extends Repository<Integer> {
        public int end = 0;

        @Override
        protected void updateEnd(Integer newInstance) {
            end += 5;
        }
    }
}