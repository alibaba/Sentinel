package com.alibaba.jvm.sandbox.qatest.core.util;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.junit.Assert.assertEquals;

public class AssertUtils {

    public static <E> void assertArrayEquals(E[] exceptArray, E[] actualArray) {
        assertEquals(
                String.format(
                        "except size not matched!\n\tEXCEPT:\n\t\t%s\n\n\tACTUAL:\n\t\t%s",
                        StringUtils.join(exceptArray, ",\n\t\t"),
                        StringUtils.join(actualArray, ",\n\t\t")
                ),
                getLength(exceptArray),
                getLength(actualArray)
        );
        for (int index = 0; index < exceptArray.length; index++) {
            assertEquals("[" + index + "] not matched", exceptArray[index], actualArray[index]);
        }
    }

}
