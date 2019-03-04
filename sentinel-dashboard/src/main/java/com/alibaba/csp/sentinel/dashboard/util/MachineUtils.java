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
package com.alibaba.csp.sentinel.dashboard.util;

import java.util.Optional;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Tuple2;

/**
 * @author Eric Zhao
 */
public final class MachineUtils {

    public static Optional<Integer> parseCommandPort(String machineIp) {
        try {
            if (!machineIp.contains("@")) {
                return Optional.empty();
            }
            String[] str = machineIp.split("@");
            if (str.length <= 1) {
                return Optional.empty();
            }
            return Optional.of(Integer.parseInt(str[1]));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static Optional<Tuple2<String, Integer>> parseCommandIpAndPort(String machineIp) {
        try {
            if (StringUtil.isEmpty(machineIp) || !machineIp.contains("@")) {
                return Optional.empty();
            }
            String[] str = machineIp.split("@");
            if (str.length <= 1) {
                return Optional.empty();
            }
            return Optional.of(Tuple2.of(str[0], Integer.parseInt(str[1])));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private MachineUtils() {}
}
