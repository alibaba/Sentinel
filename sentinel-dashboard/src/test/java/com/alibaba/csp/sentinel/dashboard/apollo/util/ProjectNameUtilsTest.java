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
package com.alibaba.csp.sentinel.dashboard.apollo.util;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author wxq
 */
public class ProjectNameUtilsTest {

    @Test
    public void getEffectiveProjectNames() {

        // empty
        assertEquals(Collections.EMPTY_SET, ProjectNameUtils.getEffectiveProjectNames("  "));
        assertEquals(Collections.EMPTY_SET, ProjectNameUtils.getEffectiveProjectNames("\n \r\n  "));
        assertEquals(Collections.EMPTY_SET, ProjectNameUtils.getEffectiveProjectNames(" \t "));
        assertEquals(Collections.EMPTY_SET, ProjectNameUtils.getEffectiveProjectNames(" \n \b \t "));

        // 1 element
        assertEquals(Collections.singleton("a"), ProjectNameUtils.getEffectiveProjectNames("a"));
        assertEquals(Collections.singleton("a b"), ProjectNameUtils.getEffectiveProjectNames("a b "));
        assertEquals(Collections.singleton("abcd234"), ProjectNameUtils.getEffectiveProjectNames("abcd234"));
        assertEquals(Collections.singleton("1"), ProjectNameUtils.getEffectiveProjectNames(" \t1\n"));
        assertEquals(Collections.singleton("2"), ProjectNameUtils.getEffectiveProjectNames("\n\r\n2\n"));

        // 2 elements
        assertEquals(2, ProjectNameUtils.getEffectiveProjectNames("a\nb").size());
        assertEquals(2, ProjectNameUtils.getEffectiveProjectNames("a\r\nb").size());
        assertEquals(2, ProjectNameUtils.getEffectiveProjectNames("\ta\nb").size());
        assertEquals(2, ProjectNameUtils.getEffectiveProjectNames("\r\na\n\tb\t").size());
    }

}