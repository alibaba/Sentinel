/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.codec.digest;

import java.util.Random;

import org.apache.commons.codec.Charsets;

/**
 * Unix crypt(3) algorithm implementation.
 * <p>
 * This class only implements the traditional 56 bit DES based algorithm. Please use DigestUtils.crypt() for a method
 * that distinguishes between all the algorithms supported in the current glibc's crypt().
 * <p>
 * The Java implementation was taken from the JetSpeed Portal project (see
 * org.apache.jetspeed.services.security.ldap.UnixCrypt).
 * <p>
 * This class is slightly incompatible if the given salt contains characters that are not part of the allowed range
 * [a-zA-Z0-9./].
 * <p>
 * This class is immutable and thread-safe.
 *
 * @version $Id: UnixCrypt.java 1429868 2013-01-07 16:08:05Z ggregory $
 * @since 1.7
 */
public class UnixCrypt {

    private static final int CON_SALT[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 5, 6,
            7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33,
            34, 35, 36, 37, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
            54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 0, 0, 0, 0 };

    private static final int COV2CHAR[] = { 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70,
            71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102,
            103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122 };

    private static final char SALT_CHARS[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789./"
            .toCharArray();

    private static final boolean SHIFT2[] = { false, false, true, true, true, true, true, true, false, true, true,
            true, true, true, true, false };

    private static final int SKB[][] = {
            { 0, 16, 0x20000000, 0x20000010, 0x10000, 0x10010, 0x20010000, 0x20010010, 2048, 2064, 0x20000800,
                    0x20000810, 0x10800, 0x10810, 0x20010800, 0x20010810, 32, 48, 0x20000020, 0x20000030, 0x10020,
                    0x10030, 0x20010020, 0x20010030, 2080, 2096, 0x20000820, 0x20000830, 0x10820, 0x10830, 0x20010820,
                    0x20010830, 0x80000, 0x80010, 0x20080000, 0x20080010, 0x90000, 0x90010, 0x20090000, 0x20090010,
                    0x80800, 0x80810, 0x20080800, 0x20080810, 0x90800, 0x90810, 0x20090800, 0x20090810, 0x80020,
                    0x80030, 0x20080020, 0x20080030, 0x90020, 0x90030, 0x20090020, 0x20090030, 0x80820, 0x80830,
                    0x20080820, 0x20080830, 0x90820, 0x90830, 0x20090820, 0x20090830 },
            { 0, 0x2000000, 8192, 0x2002000, 0x200000, 0x2200000, 0x202000, 0x2202000, 4, 0x2000004, 8196, 0x2002004,
                    0x200004, 0x2200004, 0x202004, 0x2202004, 1024, 0x2000400, 9216, 0x2002400, 0x200400, 0x2200400,
                    0x202400, 0x2202400, 1028, 0x2000404, 9220, 0x2002404, 0x200404, 0x2200404, 0x202404, 0x2202404,
                    0x10000000, 0x12000000, 0x10002000, 0x12002000, 0x10200000, 0x12200000, 0x10202000, 0x12202000,
                    0x10000004, 0x12000004, 0x10002004, 0x12002004, 0x10200004, 0x12200004, 0x10202004, 0x12202004,
                    0x10000400, 0x12000400, 0x10002400, 0x12002400, 0x10200400, 0x12200400, 0x10202400, 0x12202400,
                    0x10000404, 0x12000404, 0x10002404, 0x12002404, 0x10200404, 0x12200404, 0x10202404, 0x12202404 },
            { 0, 1, 0x40000, 0x40001, 0x1000000, 0x1000001, 0x1040000, 0x1040001, 2, 3, 0x40002, 0x40003, 0x1000002,
                    0x1000003, 0x1040002, 0x1040003, 512, 513, 0x40200, 0x40201, 0x1000200, 0x1000201, 0x1040200,
                    0x1040201, 514, 515, 0x40202, 0x40203, 0x1000202, 0x1000203, 0x1040202, 0x1040203, 0x8000000,
                    0x8000001, 0x8040000, 0x8040001, 0x9000000, 0x9000001, 0x9040000, 0x9040001, 0x8000002, 0x8000003,
                    0x8040002, 0x8040003, 0x9000002, 0x9000003, 0x9040002, 0x9040003, 0x8000200, 0x8000201, 0x8040200,
                    0x8040201, 0x9000200, 0x9000201, 0x9040200, 0x9040201, 0x8000202, 0x8000203, 0x8040202, 0x8040203,
                    0x9000202, 0x9000203, 0x9040202, 0x9040203 },
            { 0, 0x100000, 256, 0x100100, 8, 0x100008, 264, 0x100108, 4096, 0x101000, 4352, 0x101100, 4104, 0x101008,
                    4360, 0x101108, 0x4000000, 0x4100000, 0x4000100, 0x4100100, 0x4000008, 0x4100008, 0x4000108,
                    0x4100108, 0x4001000, 0x4101000, 0x4001100, 0x4101100, 0x4001008, 0x4101008, 0x4001108, 0x4101108,
                    0x20000, 0x120000, 0x20100, 0x120100, 0x20008, 0x120008, 0x20108, 0x120108, 0x21000, 0x121000,
                    0x21100, 0x121100, 0x21008, 0x121008, 0x21108, 0x121108, 0x4020000, 0x4120000, 0x4020100,
                    0x4120100, 0x4020008, 0x4120008, 0x4020108, 0x4120108, 0x4021000, 0x4121000, 0x4021100, 0x4121100,
                    0x4021008, 0x4121008, 0x4021108, 0x4121108 },
            { 0, 0x10000000, 0x10000, 0x10010000, 4, 0x10000004, 0x10004, 0x10010004, 0x20000000, 0x30000000,
                    0x20010000, 0x30010000, 0x20000004, 0x30000004, 0x20010004, 0x30010004, 0x100000, 0x10100000,
                    0x110000, 0x10110000, 0x100004, 0x10100004, 0x110004, 0x10110004, 0x20100000, 0x30100000,
                    0x20110000, 0x30110000, 0x20100004, 0x30100004, 0x20110004, 0x30110004, 4096, 0x10001000, 0x11000,
                    0x10011000, 4100, 0x10001004, 0x11004, 0x10011004, 0x20001000, 0x30001000, 0x20011000, 0x30011000,
                    0x20001004, 0x30001004, 0x20011004, 0x30011004, 0x101000, 0x10101000, 0x111000, 0x10111000,
                    0x101004, 0x10101004, 0x111004, 0x10111004, 0x20101000, 0x30101000, 0x20111000, 0x30111000,
                    0x20101004, 0x30101004, 0x20111004, 0x30111004 },
            { 0, 0x8000000, 8, 0x8000008, 1024, 0x8000400, 1032, 0x8000408, 0x20000, 0x8020000, 0x20008, 0x8020008,
                    0x20400, 0x8020400, 0x20408, 0x8020408, 1, 0x8000001, 9, 0x8000009, 1025, 0x8000401, 1033,
                    0x8000409, 0x20001, 0x8020001, 0x20009, 0x8020009, 0x20401, 0x8020401, 0x20409, 0x8020409,
                    0x2000000, 0xa000000, 0x2000008, 0xa000008, 0x2000400, 0xa000400, 0x2000408, 0xa000408, 0x2020000,
                    0xa020000, 0x2020008, 0xa020008, 0x2020400, 0xa020400, 0x2020408, 0xa020408, 0x2000001, 0xa000001,
                    0x2000009, 0xa000009, 0x2000401, 0xa000401, 0x2000409, 0xa000409, 0x2020001, 0xa020001, 0x2020009,
                    0xa020009, 0x2020401, 0xa020401, 0x2020409, 0xa020409 },
            { 0, 256, 0x80000, 0x80100, 0x1000000, 0x1000100, 0x1080000, 0x1080100, 16, 272, 0x80010, 0x80110,
                    0x1000010, 0x1000110, 0x1080010, 0x1080110, 0x200000, 0x200100, 0x280000, 0x280100, 0x1200000,
                    0x1200100, 0x1280000, 0x1280100, 0x200010, 0x200110, 0x280010, 0x280110, 0x1200010, 0x1200110,
                    0x1280010, 0x1280110, 512, 768, 0x80200, 0x80300, 0x1000200, 0x1000300, 0x1080200, 0x1080300, 528,
                    784, 0x80210, 0x80310, 0x1000210, 0x1000310, 0x1080210, 0x1080310, 0x200200, 0x200300, 0x280200,
                    0x280300, 0x1200200, 0x1200300, 0x1280200, 0x1280300, 0x200210, 0x200310, 0x280210, 0x280310,
                    0x1200210, 0x1200310, 0x1280210, 0x1280310 },
            { 0, 0x4000000, 0x40000, 0x4040000, 2, 0x4000002, 0x40002, 0x4040002, 8192, 0x4002000, 0x42000, 0x4042000,
                    8194, 0x4002002, 0x42002, 0x4042002, 32, 0x4000020, 0x40020, 0x4040020, 34, 0x4000022, 0x40022,
                    0x4040022, 8224, 0x4002020, 0x42020, 0x4042020, 8226, 0x4002022, 0x42022, 0x4042022, 2048,
                    0x4000800, 0x40800, 0x4040800, 2050, 0x4000802, 0x40802, 0x4040802, 10240, 0x4002800, 0x42800,
                    0x4042800, 10242, 0x4002802, 0x42802, 0x4042802, 2080, 0x4000820, 0x40820, 0x4040820, 2082,
                    0x4000822, 0x40822, 0x4040822, 10272, 0x4002820, 0x42820, 0x4042820, 10274, 0x4002822, 0x42822,
                    0x4042822 } };

    private static final int SPTRANS[][] = {
            { 0x820200, 0x20000, 0x80800000, 0x80820200, 0x800000, 0x80020200, 0x80020000, 0x80800000, 0x80020200,
                    0x820200, 0x820000, 0x80000200, 0x80800200, 0x800000, 0, 0x80020000, 0x20000, 0x80000000,
                    0x800200, 0x20200, 0x80820200, 0x820000, 0x80000200, 0x800200, 0x80000000, 512, 0x20200,
                    0x80820000, 512, 0x80800200, 0x80820000, 0, 0, 0x80820200, 0x800200, 0x80020000, 0x820200,
                    0x20000, 0x80000200, 0x800200, 0x80820000, 512, 0x20200, 0x80800000, 0x80020200, 0x80000000,
                    0x80800000, 0x820000, 0x80820200, 0x20200, 0x820000, 0x80800200, 0x800000, 0x80000200, 0x80020000,
                    0, 0x20000, 0x800000, 0x80800200, 0x820200, 0x80000000, 0x80820000, 512, 0x80020200 },
            { 0x10042004, 0, 0x42000, 0x10040000, 0x10000004, 8196, 0x10002000, 0x42000, 8192, 0x10040004, 4,
                    0x10002000, 0x40004, 0x10042000, 0x10040000, 4, 0x40000, 0x10002004, 0x10040004, 8192, 0x42004,
                    0x10000000, 0, 0x40004, 0x10002004, 0x42004, 0x10042000, 0x10000004, 0x10000000, 0x40000, 8196,
                    0x10042004, 0x40004, 0x10042000, 0x10002000, 0x42004, 0x10042004, 0x40004, 0x10000004, 0,
                    0x10000000, 8196, 0x40000, 0x10040004, 8192, 0x10000000, 0x42004, 0x10002004, 0x10042000, 8192, 0,
                    0x10000004, 4, 0x10042004, 0x42000, 0x10040000, 0x10040004, 0x40000, 8196, 0x10002000, 0x10002004,
                    4, 0x10040000, 0x42000 },
            { 0x41000000, 0x1010040, 64, 0x41000040, 0x40010000, 0x1000000, 0x41000040, 0x10040, 0x1000040, 0x10000,
                    0x1010000, 0x40000000, 0x41010040, 0x40000040, 0x40000000, 0x41010000, 0, 0x40010000, 0x1010040,
                    64, 0x40000040, 0x41010040, 0x10000, 0x41000000, 0x41010000, 0x1000040, 0x40010040, 0x1010000,
                    0x10040, 0, 0x1000000, 0x40010040, 0x1010040, 64, 0x40000000, 0x10000, 0x40000040, 0x40010000,
                    0x1010000, 0x41000040, 0, 0x1010040, 0x10040, 0x41010000, 0x40010000, 0x1000000, 0x41010040,
                    0x40000000, 0x40010040, 0x41000000, 0x1000000, 0x41010040, 0x10000, 0x1000040, 0x41000040,
                    0x10040, 0x1000040, 0, 0x41010000, 0x40000040, 0x41000000, 0x40010040, 64, 0x1010000 },
            { 0x100402, 0x4000400, 2, 0x4100402, 0, 0x4100000, 0x4000402, 0x100002, 0x4100400, 0x4000002, 0x4000000,
                    1026, 0x4000002, 0x100402, 0x100000, 0x4000000, 0x4100002, 0x100400, 1024, 2, 0x100400, 0x4000402,
                    0x4100000, 1024, 1026, 0, 0x100002, 0x4100400, 0x4000400, 0x4100002, 0x4100402, 0x100000,
                    0x4100002, 1026, 0x100000, 0x4000002, 0x100400, 0x4000400, 2, 0x4100000, 0x4000402, 0, 1024,
                    0x100002, 0, 0x4100002, 0x4100400, 1024, 0x4000000, 0x4100402, 0x100402, 0x100000, 0x4100402, 2,
                    0x4000400, 0x100402, 0x100002, 0x100400, 0x4100000, 0x4000402, 1026, 0x4000000, 0x4000002,
                    0x4100400 },
            { 0x2000000, 16384, 256, 0x2004108, 0x2004008, 0x2000100, 16648, 0x2004000, 16384, 8, 0x2000008, 16640,
                    0x2000108, 0x2004008, 0x2004100, 0, 16640, 0x2000000, 16392, 264, 0x2000100, 16648, 0, 0x2000008,
                    8, 0x2000108, 0x2004108, 16392, 0x2004000, 256, 264, 0x2004100, 0x2004100, 0x2000108, 16392,
                    0x2004000, 16384, 8, 0x2000008, 0x2000100, 0x2000000, 16640, 0x2004108, 0, 16648, 0x2000000, 256,
                    16392, 0x2000108, 256, 0, 0x2004108, 0x2004008, 0x2004100, 264, 16384, 16640, 0x2004008,
                    0x2000100, 264, 8, 16648, 0x2004000, 0x2000008 },
            { 0x20000010, 0x80010, 0, 0x20080800, 0x80010, 2048, 0x20000810, 0x80000, 2064, 0x20080810, 0x80800,
                    0x20000000, 0x20000800, 0x20000010, 0x20080000, 0x80810, 0x80000, 0x20000810, 0x20080010, 0, 2048,
                    16, 0x20080800, 0x20080010, 0x20080810, 0x20080000, 0x20000000, 2064, 16, 0x80800, 0x80810,
                    0x20000800, 2064, 0x20000000, 0x20000800, 0x80810, 0x20080800, 0x80010, 0, 0x20000800, 0x20000000,
                    2048, 0x20080010, 0x80000, 0x80010, 0x20080810, 0x80800, 16, 0x20080810, 0x80800, 0x80000,
                    0x20000810, 0x20000010, 0x20080000, 0x80810, 0, 2048, 0x20000010, 0x20000810, 0x20080800,
                    0x20080000, 2064, 16, 0x20080010 },
            { 4096, 128, 0x400080, 0x400001, 0x401081, 4097, 4224, 0, 0x400000, 0x400081, 129, 0x401000, 1, 0x401080,
                    0x401000, 129, 0x400081, 4096, 4097, 0x401081, 0, 0x400080, 0x400001, 4224, 0x401001, 4225,
                    0x401080, 1, 4225, 0x401001, 128, 0x400000, 4225, 0x401000, 0x401001, 129, 4096, 128, 0x400000,
                    0x401001, 0x400081, 4225, 4224, 0, 128, 0x400001, 1, 0x400080, 0, 0x400081, 0x400080, 4224, 129,
                    4096, 0x401081, 0x400000, 0x401080, 1, 4097, 0x401081, 0x400001, 0x401080, 0x401000, 4097 },
            { 0x8200020, 0x8208000, 32800, 0, 0x8008000, 0x200020, 0x8200000, 0x8208020, 32, 0x8000000, 0x208000,
                    32800, 0x208020, 0x8008020, 0x8000020, 0x8200000, 32768, 0x208020, 0x200020, 0x8008000, 0x8208020,
                    0x8000020, 0, 0x208000, 0x8000000, 0x200000, 0x8008020, 0x8200020, 0x200000, 32768, 0x8208000, 32,
                    0x200000, 32768, 0x8000020, 0x8208020, 32800, 0x8000000, 0, 0x208000, 0x8200020, 0x8008020,
                    0x8008000, 0x200020, 0x8208000, 32, 0x200020, 0x8008000, 0x8208020, 0x200000, 0x8200000,
                    0x8000020, 0x208000, 32800, 0x8008020, 0x8200000, 32, 0x8208000, 0x208020, 0, 0x8000000,
                    0x8200020, 32768, 0x208020 } };

    /**
     * Generates a crypt(3) compatible hash using the DES algorithm.
     * <p>
     * As no salt is given, a random one will be used.
     *
     * @param original
     *            plaintext password
     * @return a 13 character string starting with the salt string
     */
    public static String crypt(final byte[] original) {
        return crypt(original, null);
    }

    /**
     * Generates a crypt(3) compatible hash using the DES algorithm.
     * <p>
     * Using unspecified characters as salt results incompatible hash values.
     *
     * @param original
     *            plaintext password
     * @param salt
     *            a two character string drawn from [a-zA-Z0-9./] or null for a random one
     * @return a 13 character string starting with the salt string
     * @throws IllegalArgumentException
     *             if the salt does not match the allowed pattern
     */
    public static String crypt(final byte[] original, String salt) {
        if (salt == null) {
            final Random randomGenerator = new Random();
            final int numSaltChars = SALT_CHARS.length;
            salt = "" + SALT_CHARS[randomGenerator.nextInt(numSaltChars)] +
                    SALT_CHARS[randomGenerator.nextInt(numSaltChars)];
        } else if (!salt.matches("^[" + B64.B64T + "]{2,}$")) {
            throw new IllegalArgumentException("Invalid salt value: " + salt);
        }

        final StringBuilder buffer = new StringBuilder("             ");
        final char charZero = salt.charAt(0);
        final char charOne = salt.charAt(1);
        buffer.setCharAt(0, charZero);
        buffer.setCharAt(1, charOne);
        final int eSwap0 = CON_SALT[charZero];
        final int eSwap1 = CON_SALT[charOne] << 4;
        final byte key[] = new byte[8];
        for (int i = 0; i < key.length; i++) {
            key[i] = 0;
        }

        for (int i = 0; i < key.length && i < original.length; i++) {
            final int iChar = original[i];
            key[i] = (byte) (iChar << 1);
        }

        final int schedule[] = desSetKey(key);
        final int out[] = body(schedule, eSwap0, eSwap1);
        final byte b[] = new byte[9];
        intToFourBytes(out[0], b, 0);
        intToFourBytes(out[1], b, 4);
        b[8] = 0;
        int i = 2;
        int y = 0;
        int u = 128;
        for (; i < 13; i++) {
            int j = 0;
            int c = 0;
            for (; j < 6; j++) {
                c <<= 1;
                if ((b[y] & u) != 0) {
                    c |= 0x1;
                }
                u >>>= 1;
                if (u == 0) {
                    y++;
                    u = 128;
                }
                buffer.setCharAt(i, (char) COV2CHAR[c]);
            }
        }
        return buffer.toString();
    }

    /**
     * Generates a crypt(3) compatible hash using the DES algorithm.
     * <p>
     * As no salt is given, a random one is used.
     *
     * @param original
     *            plaintext password
     * @return a 13 character string starting with the salt string
     */
    public static String crypt(final String original) {
        return crypt(original.getBytes(Charsets.UTF_8));
    }

    /**
     * Generates a crypt(3) compatible hash using the DES algorithm.
     *
     * @param original
     *            plaintext password
     * @param salt
     *            a two character string drawn from [a-zA-Z0-9./] or null for a random one
     * @return a 13 character string starting with the salt string
     * @throws IllegalArgumentException
     *             if the salt does not match the allowed pattern
     */
    public static String crypt(final String original, final String salt) {
        return crypt(original.getBytes(Charsets.UTF_8), salt);
    }

    private static int[] body(final int schedule[], final int eSwap0, final int eSwap1) {
        int left = 0;
        int right = 0;
        int t = 0;
        for (int j = 0; j < 25; j++) {
            for (int i = 0; i < 32; i += 4) {
                left = dEncrypt(left, right, i, eSwap0, eSwap1, schedule);
                right = dEncrypt(right, left, i + 2, eSwap0, eSwap1, schedule);
            }
            t = left;
            left = right;
            right = t;
        }

        t = right;
        right = left >>> 1 | left << 31;
        left = t >>> 1 | t << 31;
        final int results[] = new int[2];
        permOp(right, left, 1, 0x55555555, results);
        right = results[0];
        left = results[1];
        permOp(left, right, 8, 0xff00ff, results);
        left = results[0];
        right = results[1];
        permOp(right, left, 2, 0x33333333, results);
        right = results[0];
        left = results[1];
        permOp(left, right, 16, 65535, results);
        left = results[0];
        right = results[1];
        permOp(right, left, 4, 0xf0f0f0f, results);
        right = results[0];
        left = results[1];
        final int out[] = new int[2];
        out[0] = left;
        out[1] = right;
        return out;
    }

    private static int byteToUnsigned(final byte b) {
        final int value = b;
        return value < 0 ? value + 256 : value;
    }

    private static int dEncrypt(int el, final int r, final int s, final int e0, final int e1, final int sArr[]) {
        int v = r ^ r >>> 16;
        int u = v & e0;
        v &= e1;
        u = u ^ u << 16 ^ r ^ sArr[s];
        int t = v ^ v << 16 ^ r ^ sArr[s + 1];
        t = t >>> 4 | t << 28;
        el ^= SPTRANS[1][t & 0x3f] | SPTRANS[3][t >>> 8 & 0x3f] | SPTRANS[5][t >>> 16 & 0x3f] |
                SPTRANS[7][t >>> 24 & 0x3f] | SPTRANS[0][u & 0x3f] | SPTRANS[2][u >>> 8 & 0x3f] |
                SPTRANS[4][u >>> 16 & 0x3f] | SPTRANS[6][u >>> 24 & 0x3f];
        return el;
    }

    private static int[] desSetKey(final byte key[]) {
        final int schedule[] = new int[32];
        int c = fourBytesToInt(key, 0);
        int d = fourBytesToInt(key, 4);
        final int results[] = new int[2];
        permOp(d, c, 4, 0xf0f0f0f, results);
        d = results[0];
        c = results[1];
        c = hPermOp(c, -2, 0xcccc0000);
        d = hPermOp(d, -2, 0xcccc0000);
        permOp(d, c, 1, 0x55555555, results);
        d = results[0];
        c = results[1];
        permOp(c, d, 8, 0xff00ff, results);
        c = results[0];
        d = results[1];
        permOp(d, c, 1, 0x55555555, results);
        d = results[0];
        c = results[1];
        d = (d & 0xff) << 16 | d & 0xff00 | (d & 0xff0000) >>> 16 | (c & 0xf0000000) >>> 4;
        c &= 0xfffffff;
        int j = 0;
        for (int i = 0; i < 16; i++) {
            if (SHIFT2[i]) {
                c = c >>> 2 | c << 26;
                d = d >>> 2 | d << 26;
            } else {
                c = c >>> 1 | c << 27;
                d = d >>> 1 | d << 27;
            }
            c &= 0xfffffff;
            d &= 0xfffffff;
            int s = SKB[0][c & 0x3f] | SKB[1][c >>> 6 & 0x3 | c >>> 7 & 0x3c] |
                    SKB[2][c >>> 13 & 0xf | c >>> 14 & 0x30] |
                    SKB[3][c >>> 20 & 0x1 | c >>> 21 & 0x6 | c >>> 22 & 0x38];
            final int t = SKB[4][d & 0x3f] | SKB[5][d >>> 7 & 0x3 | d >>> 8 & 0x3c] | SKB[6][d >>> 15 & 0x3f] |
                    SKB[7][d >>> 21 & 0xf | d >>> 22 & 0x30];
            schedule[j++] = (t << 16 | s & 0xffff);
            s = s >>> 16 | t & 0xffff0000;
            s = s << 4 | s >>> 28;
            schedule[j++] = s;
        }

        return schedule;
    }

    private static int fourBytesToInt(final byte b[], int offset) {
        int value = byteToUnsigned(b[offset++]);
        value |= byteToUnsigned(b[offset++]) << 8;
        value |= byteToUnsigned(b[offset++]) << 16;
        value |= byteToUnsigned(b[offset++]) << 24;
        return value;
    }

    private static int hPermOp(int a, final int n, final int m) {
        final int t = (a << 16 - n ^ a) & m;
        a = a ^ t ^ t >>> 16 - n;
        return a;
    }

    private static void intToFourBytes(final int iValue, final byte b[], int offset) {
        b[offset++] = (byte) (iValue & 0xff);
        b[offset++] = (byte) (iValue >>> 8 & 0xff);
        b[offset++] = (byte) (iValue >>> 16 & 0xff);
        b[offset++] = (byte) (iValue >>> 24 & 0xff);
    }

    private static void permOp(int a, int b, final int n, final int m, final int results[]) {
        final int t = (a >>> n ^ b) & m;
        a ^= t << n;
        b ^= t;
        results[0] = a;
        results[1] = b;
    }

}
