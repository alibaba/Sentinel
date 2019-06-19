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
package com.alibaba.csp.sentinel.cluster.client.codec.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientGlobalConfig;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 * @author lianglin
 */
public class ParamRequestEncoderTest {

    @Test
    public void testCalculateParamTransportSize() {
        ParamRequestEncoder encoder = new ParamRequestEncoder(ClusterClientGlobalConfig.DEFAULT_PARAM_MAX_SIZE);
        // POJO (non-primitive type) should not be regarded as a valid parameter.
        assertEquals(0, encoder.calculateParamTransportSize(new SomePojo().setParam1("abc")));

        assertEquals(4 + 1, encoder.calculateParamTransportSize(1));
        assertEquals(1 + 1, encoder.calculateParamTransportSize((byte)1));
        assertEquals(1 + 1, encoder.calculateParamTransportSize(false));
        assertEquals(8 + 1, encoder.calculateParamTransportSize(2L));
        assertEquals(8 + 1, encoder.calculateParamTransportSize(4.0d));
        final String paramStr = "Sentinel";
        assertEquals(1 + 4 + paramStr.getBytes().length, encoder.calculateParamTransportSize(paramStr));
    }

    @Test
    public void testResolveValidParams() {
        final int maxSize = 15;
        ParamRequestEncoder encoder = new ParamRequestEncoder(maxSize);

        ArrayList<Object> params = new ArrayList<Object>() {{
            add(1);
            add(64);
            add(3);
        }};

        List<Object> validParams = encoder.resolveValidParams(params);
        assertEquals(3, validParams.size());
        assertEquals(params.get(0), 1);
        assertEquals(params.get(1), 64);
        assertEquals(params.get(2), 3);

        // When exceeding maxSize, the parameter will not be included.
        params.add(5);
        validParams = encoder.resolveValidParams(params);
        assertEquals(3, validParams.size());
        assertFalse(validParams.contains(5));

        // POJO (non-primitive type) should not be regarded as a valid parameter
        assertEquals(0, encoder.resolveValidParams(new ArrayList<Object>() {{
            add(new SomePojo());
        }}).size());
    }

    @Test
    public void testResolveValidParamMap() {
        final int maxSize = 15;
        ParamRequestEncoder encoder = new ParamRequestEncoder(maxSize);

        Map<Integer, Object> paramMap = new HashMap<>();
        paramMap.put(0, 1);
        paramMap.put(2, 64);
        paramMap.put(3, 3);

        Map<Integer, Object> params = encoder.resolveValidParamMap(paramMap);
        assertEquals(3, params.size());
        assertEquals(params.get(0), 1);
        assertEquals(params.get(2), 64);
        assertEquals(params.get(3), 3);

        // When exceeding maxSize, the parameter will not be included.
        paramMap.put(4, 5L);
        params = encoder.resolveValidParamMap(paramMap);
        assertEquals(3, params.size());
        assertFalse(params.containsKey(4));

        // POJO (non-primitive type) should not be regarded as a valid parameter
        assertEquals(0, encoder.resolveValidParamMap(new HashMap<Integer, Object>() {{
            put(0, new SomePojo());
        }}).size());
    }

    private static class SomePojo {
        private String param1;

        public String getParam1() {
            return param1;
        }

        public SomePojo setParam1(String param1) {
            this.param1 = param1;
            return this;
        }

        @Override
        public String toString() {
            return "SomePojo{" +
                "param1='" + param1 + '\'' +
                '}';
        }
    }
}
