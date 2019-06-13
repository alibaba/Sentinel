package com.alibaba.csp.sentinel.cluster.client.codec.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class ParamFlowRequestDataWriterTest {

    @Test
    public void testCalculateParamTransportSize() {
        ParamFlowRequestDataWriter writer = new ParamFlowRequestDataWriter();
        // POJO (non-primitive type) should not be regarded as a valid parameter.
        assertEquals(0, writer.calculateParamTransportSize(new SomePojo().setParam1("abc")));

        assertEquals(4 + 1, writer.calculateParamTransportSize(1));
        assertEquals(1 + 1, writer.calculateParamTransportSize((byte) 1));
        assertEquals(1 + 1, writer.calculateParamTransportSize(false));
        assertEquals(8 + 1, writer.calculateParamTransportSize(2L));
        assertEquals(8 + 1, writer.calculateParamTransportSize(4.0d));
        final String paramStr = "Sentinel";
        assertEquals(1 + 4 + paramStr.getBytes().length, writer.calculateParamTransportSize(paramStr));
    }

    @Test
    public void testResolveValidParams() {

        final int maxSize = 15;
        ParamFlowRequestDataWriter writer = new ParamFlowRequestDataWriter(maxSize);

        ArrayList<Object> params = new ArrayList<Object>() {{
            add(1);
            add(64);
            add(3);
        }};

        List<Object> validParams = writer.resolveValidParams(params);
        assertTrue(validParams.contains(1) && validParams.contains(64) && validParams.contains(3));

        //when over maxSize, the exceed number should not be contained
        params.add(5);
        assertFalse(writer.resolveValidParams(params).contains(5));


        //POJO (non-primitive type) should not be regarded as a valid parameter
        assertTrue(writer.resolveValidParams(new ArrayList<Object>() {{
            add(new SomePojo());
        }}).size() == 0);

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