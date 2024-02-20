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
package com.alibaba.csp.sentinel.cluster.server;

import com.alibaba.csp.sentinel.util.TimeUtil;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Mock support for {@link TimeUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractTimeBasedTest {

    private long currentMillis = 0;

    public MockedStatic<TimeUtil> mockTimeUtil() {
        MockedStatic<TimeUtil> mocked = Mockito.mockStatic(TimeUtil.class);
        mocked.when(TimeUtil::currentTimeMillis).thenReturn(currentMillis);
        return mocked;
    }

    protected final void useActualTime(MockedStatic<TimeUtil> mocked) {
        mocked.when(TimeUtil::currentTimeMillis).thenCallRealMethod();
    }

    protected final void setCurrentMillis(MockedStatic<TimeUtil> mocked, long cur) {
        currentMillis = cur;
        mocked.when(TimeUtil::currentTimeMillis).thenReturn(currentMillis);
    }

    protected final void sleep(MockedStatic<TimeUtil> mocked, long t) {
        currentMillis += t;
        mocked.when(TimeUtil::currentTimeMillis).thenReturn(currentMillis);
    }

    protected final void sleepSecond(MockedStatic<TimeUtil> mocked, long timeSec) {
        sleep(mocked, timeSec * 1000);
    }
}
