/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.acm.shaded.com.aliyuncs.utils;

import java.io.UnsupportedEncodingException;

public class Base64Helper {

    private static final String BASE64_CODE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        + "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "+/";

    private static final int[] BASE64_DECODE = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
        -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
        -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    };

    private static byte[] zeroPad(int length, byte[] bytes) {
        byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    public synchronized static String encode(byte[] buff) {
        if (null == buff) { return null; }

        StringBuilder strBuilder = new StringBuilder("");
        int paddingCount = (3 - (buff.length % 3)) % 3;
        byte[] stringArray = zeroPad(buff.length + paddingCount, buff);
        for (int i = 0; i < stringArray.length; i += 3) {
            int j = ((stringArray[i] & 0xff) << 16) +
                ((stringArray[i + 1] & 0xff) << 8) +
                (stringArray[i + 2] & 0xff);
            strBuilder.append(BASE64_CODE.charAt((j >> 18) & 0x3f));
            strBuilder.append(BASE64_CODE.charAt((j >> 12) & 0x3f));
            strBuilder.append(BASE64_CODE.charAt((j >> 6) & 0x3f));
            strBuilder.append(BASE64_CODE.charAt(j & 0x3f));
        }
        int intPos = strBuilder.length();
        for (int i = paddingCount; i > 0; i--) {
            strBuilder.setCharAt(intPos - i, '=');
        }

        return strBuilder.toString();
    }

    public synchronized static String encode(String string, String encoding)
        throws UnsupportedEncodingException {
        if (null == string || null == encoding) { return null; }
        byte[] stringArray = string.getBytes(encoding);
        return encode(stringArray);
    }

    public synchronized static String decode(String string, String encoding) throws
        UnsupportedEncodingException {
        if (null == string || null == encoding) { return null; }
        int posIndex = 0;
        int decodeLen = string.endsWith("==") ? (string.length() - 2) :
            string.endsWith("=") ? (string.length() - 1) : string.length();
        byte[] buff = new byte[decodeLen * 3 / 4];
        int count4 = decodeLen - decodeLen % 4;
        for (int i = 0; i < count4; i += 4) {
            int c0 = BASE64_DECODE[string.charAt(i)];
            int c1 = BASE64_DECODE[string.charAt(i + 1)];
            int c2 = BASE64_DECODE[string.charAt(i + 2)];
            int c3 = BASE64_DECODE[string.charAt(i + 3)];
            buff[posIndex++] = (byte)(((c0 << 2) | (c1 >> 4)) & 0xFF);
            buff[posIndex++] = (byte)((((c1 & 0xF) << 4) | (c2 >> 2)) & 0xFF);
            buff[posIndex++] = (byte)((((c2 & 3) << 6) | c3) & 0xFF);
        }
        if (2 <= decodeLen % 4) {
            int c0 = BASE64_DECODE[string.charAt(count4)];
            int c1 = BASE64_DECODE[string.charAt(count4 + 1)];
            buff[posIndex++] = (byte)(((c0 << 2) | (c1 >> 4)) & 0xFF);
            if (3 == decodeLen % 4) {
                int c2 = BASE64_DECODE[string.charAt(count4 + 2)];
                buff[posIndex++] = (byte)((((c1 & 0xF) << 4) | (c2 >> 2)) & 0xFF);
            }
        }
        return new String(buff, encoding);
    }
}
