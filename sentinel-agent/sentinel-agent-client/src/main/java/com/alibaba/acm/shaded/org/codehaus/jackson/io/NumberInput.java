package com.alibaba.acm.shaded.org.codehaus.jackson.io;

public final class NumberInput
{
    /**
     * Textual representation of a double constant that can cause nasty problems
     * with JDK (see http://www.exploringbinary.com/java-hangs-when-converting-2-2250738585072012e-308).
     */
    public final static String NASTY_SMALL_DOUBLE = "2.2250738585072012e-308";

    /**
     * Constants needed for parsing longs from basic int parsing methods
     */
    final static long L_BILLION = 1000000000;

    final static String MIN_LONG_STR_NO_SIGN = String.valueOf(Long.MIN_VALUE).substring(1);
    final static String MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);
    
    /**
     * Fast method for parsing integers that are known to fit into
     * regular 32-bit signed int type. This means that length is
     * between 1 and 9 digits (inclusive)
     *<p>
     * Note: public to let unit tests call it
     */
    public final static int parseInt(char[] digitChars, int offset, int len)
    {
        int num = digitChars[offset] - '0';
        len += offset;
        // This looks ugly, but appears the fastest way (as per measurements)
        if (++offset < len) {
            num = (num * 10) + (digitChars[offset] - '0');
            if (++offset < len) {
                num = (num * 10) + (digitChars[offset] - '0');
                if (++offset < len) {
                    num = (num * 10) + (digitChars[offset] - '0');
                    if (++offset < len) {
                        num = (num * 10) + (digitChars[offset] - '0');
                        if (++offset < len) {
                            num = (num * 10) + (digitChars[offset] - '0');
                            if (++offset < len) {
                                num = (num * 10) + (digitChars[offset] - '0');
                                if (++offset < len) {
                                    num = (num * 10) + (digitChars[offset] - '0');
                                    if (++offset < len) {
                                        num = (num * 10) + (digitChars[offset] - '0');
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return num;
    }

    /**
     * Helper method to (more) efficiently parse integer numbers from
     * String values.
     * 
     * @since 1.7
     */
    public final static int parseInt(String str)
    {
        /* Ok: let's keep strategy simple: ignoring optional minus sign,
         * we'll accept 1 - 9 digits and parse things efficiently;
         * otherwise just defer to JDK parse functionality.
         */
        char c = str.charAt(0);
        int length = str.length();
        boolean negative = (c == '-');
        int offset = 1;
        // must have 1 - 9 digits after optional sign:
        // negative?
        if (negative) {
            if (length == 1 || length > 10) {
                return Integer.parseInt(str);
            }
            c = str.charAt(offset++);
        } else {
            if (length > 9) {
                return Integer.parseInt(str);
            }
        }
        if (c > '9' || c < '0') {
            return Integer.parseInt(str);
        }
        int num = c - '0';
        if (offset < length) {
            c = str.charAt(offset++);
            if (c > '9' || c < '0') {
                return Integer.parseInt(str);
            }
            num = (num * 10) + (c - '0');
            if (offset < length) {
                c = str.charAt(offset++);
                if (c > '9' || c < '0') {
                    return Integer.parseInt(str);
                }
                num = (num * 10) + (c - '0');
                // Let's just loop if we have more than 3 digits:
                if (offset < length) {
                    do {
                        c = str.charAt(offset++);
                        if (c > '9' || c < '0') {
                            return Integer.parseInt(str);
                        }
                        num = (num * 10) + (c - '0');
                    } while (offset < length);
                }
            }
        }
        return negative ? -num : num;
    }
    
    public final static long parseLong(char[] digitChars, int offset, int len)
    {
        // Note: caller must ensure length is [10, 18]
        int len1 = len-9;
        long val = parseInt(digitChars, offset, len1) * L_BILLION;
        return val + (long) parseInt(digitChars, offset+len1, 9);
    }

    public final static long parseLong(String str)
    {
        /* Ok, now; as the very first thing, let's just optimize case of "fake longs";
         * that is, if we know they must be ints, call int parsing
         */
        int length = str.length();
        if (length <= 9) {
            return (long) parseInt(str);
        }
        // !!! TODO: implement efficient 2-int parsing...
        return Long.parseLong(str);
    }
    
    /**
     * Helper method for determining if given String representation of
     * an integral number would fit in 64-bit Java long or not.
     * Note that input String must NOT contain leading minus sign (even
     * if 'negative' is set to true).
     *
     * @param negative Whether original number had a minus sign (which is
     *    NOT passed to this method) or not
     */
    public final static boolean inLongRange(char[] digitChars, int offset, int len,
            boolean negative)
    {
        String cmpStr = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmpStr.length();
        if (len < cmpLen) return true;
        if (len > cmpLen) return false;

        for (int i = 0; i < cmpLen; ++i) {
            int diff = digitChars[offset+i] - cmpStr.charAt(i);
            if (diff != 0) {
                return (diff < 0);
            }
        }
        return true;
    }

    /**
     * Similar to {@link #inLongRange(char[],int,int,boolean)}, but
     * with String argument
     *
     * @param negative Whether original number had a minus sign (which is
     *    NOT passed to this method) or not
     *
     * @since 1.5.0
     */
    public final static boolean inLongRange(String numberStr, boolean negative)
    {
        String cmpStr = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmpStr.length();
        int actualLen = numberStr.length();
        if (actualLen < cmpLen) return true;
        if (actualLen > cmpLen) return false;

        // could perhaps just use String.compareTo()?
        for (int i = 0; i < cmpLen; ++i) {
            int diff = numberStr.charAt(i) - cmpStr.charAt(i);
            if (diff != 0) {
                return (diff < 0);
            }
        }
        return true;
    }

    /**
     * @since 1.6
     */
    public static int parseAsInt(String input, int defaultValue)
    {
        if (input == null) {
            return defaultValue;
        }
        input = input.trim();
        int len = input.length();
        if (len == 0) {
            return defaultValue;
        }
        // One more thing: use integer parsing for 'simple'
        int i = 0;
        if (i < len) { // skip leading sign:
            char c = input.charAt(0);
            if (c == '+') { // for plus, actually physically remove
                input = input.substring(1);
                len = input.length();
            } else if (c == '-') { // minus, just skip for checks, must retain
                ++i;
            }
        }
        for (; i < len; ++i) {
            char c = input.charAt(i);
            // if other symbols, parse as Double, coerce
            if (c > '9' || c < '0') {
                try {
                    return (int) parseDouble(input);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) { }
        return defaultValue;
    }

    /**
     * @since 1.6
     */
    public static long parseAsLong(String input, long defaultValue)
    {
        if (input == null) {
            return defaultValue;
        }
        input = input.trim();
        int len = input.length();
        if (len == 0) {
            return defaultValue;
        }
        // One more thing: use long parsing for 'simple'
        int i = 0;
        if (i < len) { // skip leading sign:
            char c = input.charAt(0);
            if (c == '+') { // for plus, actually physically remove
                input = input.substring(1);
                len = input.length();
            } else if (c == '-') { // minus, just skip for checks, must retain
                ++i;
            }
        }
        for (; i < len; ++i) {
            char c = input.charAt(i);
            // if other symbols, parse as Double, coerce
            if (c > '9' || c < '0') {
                try {
                    return (long) parseDouble(input);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) { }
        return defaultValue;
    }
    
    /**
     * @since 1.6
     */
    public static double parseAsDouble(String input, double defaultValue)
    {
        if (input == null) {
            return defaultValue;
        }
        input = input.trim();
        int len = input.length();
        if (len == 0) {
            return defaultValue;
        }
        try {
            return parseDouble(input);
        } catch (NumberFormatException e) { }
        return defaultValue;
    }
    
    /**
     * @since 1.8
     */
    public final static double parseDouble(String numStr) throws NumberFormatException
    {
        // [JACKSON-486]: avoid some nasty float representations... but should it be MIN_NORMAL or MIN_VALUE?
        if (NASTY_SMALL_DOUBLE.equals(numStr)) {
            return Double.MIN_NORMAL;
        }
        return Double.parseDouble(numStr);
    }
}
