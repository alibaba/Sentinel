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

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wxq
 */
public class ProjectNameUtils {

    /**
     * Get multiple project names from a text.
     * <p>
     * Skip empty lines and trim every line.
     *
     * @param multipleProjectNamesTextarea text
     * @return multiple project names
     */
    public static Set<String> getEffectiveProjectNames(String multipleProjectNamesTextarea) {
        String[] lines = multipleProjectNamesTextarea.split("\\R+");
        Set<String> projectNames = Arrays.stream(lines)
                // trim every lines
                .map(String::trim)
                // skip blank lines
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        return projectNames;
    }

}
