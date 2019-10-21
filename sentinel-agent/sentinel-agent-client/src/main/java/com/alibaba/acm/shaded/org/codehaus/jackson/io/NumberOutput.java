package com.alibaba.acm.shaded.org.codehaus.jackson.io;

public final class NumberOutput
{
    private final static char NULL_CHAR = (char) 0;

    private static int MILLION = 1000000;
    private static int BILLION = 1000000000;
    private static long TEN_BILLION_L = 10000000000L;
    private static long THOUSAND_L = 1000L;

    private static long MIN_INT_AS_LONG = (long) Integer.MIN_VALUE;
    private static long MAX_INT_AS_LONG = (long) Integer.MAX_VALUE;

    final static String SMALLEST_LONG = String.valueOf(Long.MIN_VALUE);

    final static char[] LEADING_TRIPLETS = new char[4000];
    final static char[] FULL_TRIPLETS = new char[4000];
    static {
        /* Let's fill it with NULLs for ignorable leading digits,
         * and digit chars for others
         */
        int ix = 0;
        for (int i1 = 0; i1 < 10; ++i1) {
            char f1 = (char) ('0' + i1);
            char l1 = (i1 == 0) ? NULL_CHAR : f1;
            for (int i2 = 0; i2 < 10; ++i2) {
                char f2 = (char) ('0' + i2);
                char l2 = (i1 == 0 && i2 == 0) ? NULL_CHAR : f2;
                for (int i3 = 0; i3 < 10; ++i3) {
                    // Last is never to be empty
                    char f3 = (char) ('0' + i3);
                    LEADING_TRIPLETS[ix] = l1;
                    LEADING_TRIPLETS[ix+1] = l2;
                    LEADING_TRIPLETS[ix+2] = f3;
                    FULL_TRIPLETS[ix] = f1;
                    FULL_TRIPLETS[ix+1] = f2;
                    FULL_TRIPLETS[ix+2] = f3;
                    ix += 4;
                }
            }
        }
    }

    final static byte[] FULL_TRIPLETS_B = new byte[4000];
    static {
        for (int i = 0; i < 4000; ++i) {
            FULL_TRIPLETS_B[i] = (byte) FULL_TRIPLETS[i];
        }
    }
    
    final static String[] sSmallIntStrs = new String[] {
        "0","1","2","3","4","5","6","7","8","9","10"
    };
    final static String[] sSmallIntStrs2 = new String[] {
        "-1","-2","-3","-4","-5","-6","-7","-8","-9","-10"
    };

    /*
    /**********************************************************
    /* Efficient serialization methods using raw buffers
    /**********************************************************
     */

    /**
     * @return Offset within buffer after outputting int
     */
    public static int outputInt(int value, char[] buffer, int offset)
    {
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                /* Special case: no matching positive value within range;
                 * let's then "upgrade" to long and output as such.
                 */
                return outputLong((long) value, buffer, offset);
            }
            buffer[offset++] = '-';
            value = -value;
        }

        if (value < MILLION) { // at most 2 triplets...
            if (value < 1000) {
                if (value < 10) {
                    buffer[offset++] = (char) ('0' + value);
                } else {
                    offset = outputLeadingTriplet(value, buffer, offset);
                }
            } else {
                int thousands = value / 1000;
                value -= (thousands * 1000); // == value % 1000
                offset = outputLeadingTriplet(thousands, buffer, offset);
                offset = outputFullTriplet(value, buffer, offset);
            }
            return offset;
        }

        // ok, all 3 triplets included
        /* Let's first hand possible billions separately before
         * handling 3 triplets. This is possible since we know we
         * can have at most '2' as billion count.
         */
        boolean hasBillions = (value >= BILLION);
        if (hasBillions) {
            value -= BILLION;
            if (value >= BILLION) {
                value -= BILLION;
                buffer[offset++] = '2';
            } else {
                buffer[offset++] = '1';
            }
        }
        int newValue = value / 1000;
        int ones = (value - (newValue * 1000)); // == value % 1000
        value = newValue;
        newValue /= 1000;
        int thousands = (value - (newValue * 1000));
        
        // value now has millions, which have 1, 2 or 3 digits
        if (hasBillions) {
            offset = outputFullTriplet(newValue, buffer, offset);
        } else {
            offset = outputLeadingTriplet(newValue, buffer, offset);
        }
        offset = outputFullTriplet(thousands, buffer, offset);
        offset = outputFullTriplet(ones, buffer, offset);
        return offset;
    }

    public static int outputInt(int value, byte[] buffer, int offset)
    {
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                return outputLong((long) value, buffer, offset);
            }
            buffer[offset++] = '-';
            value = -value;
        }

        if (value < MILLION) { // at most 2 triplets...
            if (value < 1000) {
                if (value < 10) {
                    buffer[offset++] = (byte) ('0' + value);
                } else {
                    offset = outputLeadingTriplet(value, buffer, offset);
                }
            } else {
                int thousands = value / 1000;
                value -= (thousands * 1000); // == value % 1000
                offset = outputLeadingTriplet(thousands, buffer, offset);
                offset = outputFullTriplet(value, buffer, offset);
            }
            return offset;
        }
        boolean hasBillions = (value >= BILLION);
        if (hasBillions) {
            value -= BILLION;
            if (value >= BILLION) {
                value -= BILLION;
                buffer[offset++] = '2';
            } else {
                buffer[offset++] = '1';
            }
        }
        int newValue = value / 1000;
        int ones = (value - (newValue * 1000)); // == value % 1000
        value = newValue;
        newValue /= 1000;
        int thousands = (value - (newValue * 1000));
        
        if (hasBillions) {
            offset = outputFullTriplet(newValue, buffer, offset);
        } else {
            offset = outputLeadingTriplet(newValue, buffer, offset);
        }
        offset = outputFullTriplet(thousands, buffer, offset);
        offset = outputFullTriplet(ones, buffer, offset);
        return offset;
    }
    
    /**
     * @return Offset within buffer after outputting int
     */
    public static int outputLong(long value, char[] buffer, int offset)
    {
        // First: does it actually fit in an int?
        if (value < 0L) {
            /* MIN_INT is actually printed as long, just because its
             * negation is not an int but long
             */
            if (value > MIN_INT_AS_LONG) {
                return outputInt((int) value, buffer, offset);
            }
            if (value == Long.MIN_VALUE) {
                // Special case: no matching positive value within range
                int len = SMALLEST_LONG.length();
                SMALLEST_LONG.getChars(0, len, buffer, offset);
                return (offset + len);
            }
            buffer[offset++] = '-';
            value = -value;
        } else {
            if (value <= MAX_INT_AS_LONG) {
                return outputInt((int) value, buffer, offset);
            }
        }

        /* Ok: real long print. Need to first figure out length
         * in characters, and then print in from end to beginning
         */
        int origOffset = offset;
        offset += calcLongStrLength(value);
        int ptr = offset;

        // First, with long arithmetics:
        while (value > MAX_INT_AS_LONG) { // full triplet
            ptr -= 3;
            long newValue = value / THOUSAND_L;
            int triplet = (int) (value - newValue * THOUSAND_L);
            outputFullTriplet(triplet, buffer, ptr);
            value = newValue;
        }
        // Then with int arithmetics:
        int ivalue = (int) value;
        while (ivalue >= 1000) { // still full triplet
            ptr -= 3;
            int newValue = ivalue / 1000;
            int triplet = ivalue - (newValue * 1000);
            outputFullTriplet(triplet, buffer, ptr);
            ivalue = newValue;
        }
        // And finally, if anything remains, partial triplet
        outputLeadingTriplet(ivalue, buffer, origOffset);

        return offset;
    }

    public static int outputLong(long value, byte[] buffer, int offset)
    {
        if (value < 0L) {
            if (value > MIN_INT_AS_LONG) {
                return outputInt((int) value, buffer, offset);
            }
            if (value == Long.MIN_VALUE) {
                // Special case: no matching positive value within range
                int len = SMALLEST_LONG.length();
                for (int i = 0; i < len; ++i) {
                    buffer[offset++] = (byte) SMALLEST_LONG.charAt(i);
                }
                return offset;
            }
            buffer[offset++] = '-';
            value = -value;
        } else {
            if (value <= MAX_INT_AS_LONG) {
                return outputInt((int) value, buffer, offset);
            }
        }
        int origOffset = offset;
        offset += calcLongStrLength(value);
        int ptr = offset;

        // First, with long arithmetics:
        while (value > MAX_INT_AS_LONG) { // full triplet
            ptr -= 3;
            long newValue = value / THOUSAND_L;
            int triplet = (int) (value - newValue * THOUSAND_L);
            outputFullTriplet(triplet, buffer, ptr);
            value = newValue;
        }
        // Then with int arithmetics:
        int ivalue = (int) value;
        while (ivalue >= 1000) { // still full triplet
            ptr -= 3;
            int newValue = ivalue / 1000;
            int triplet = ivalue - (newValue * 1000);
            outputFullTriplet(triplet, buffer, ptr);
            ivalue = newValue;
        }
        outputLeadingTriplet(ivalue, buffer, origOffset);
        return offset;
    }
    
    /*
    /**********************************************************
    /* Secondary convenience serialization methods
    /**********************************************************
     */

    /* !!! 05-Aug-2008, tatus: Any ways to further optimize
     *   these? (or need: only called by diagnostics methods?)
     */

    public static String toString(int value)
    {
        // Lookup table for small values
        if (value < sSmallIntStrs.length) {
            if (value >= 0) {
                return sSmallIntStrs[value];
            }
            int v2 = -value - 1;
            if (v2 < sSmallIntStrs2.length) {
                return sSmallIntStrs2[v2];
            }
        }
        return Integer.toString(value);
    }

    public static String toString(long value)
    {
        if (value <= Integer.MAX_VALUE &&
            value >= Integer.MIN_VALUE) {
            return toString((int) value);
        }
        return Long.toString(value);
    }

    public static String toString(double value)
    {
        return Double.toString(value);
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    private static int outputLeadingTriplet(int triplet, char[] buffer, int offset)
    {
        int digitOffset = (triplet << 2);
        char c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = c;
        }
        c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = c;
        }
        // Last is required to be non-empty
        buffer[offset++] = LEADING_TRIPLETS[digitOffset];
        return offset;
    }

    private static int outputLeadingTriplet(int triplet, byte[] buffer, int offset)
    {
        int digitOffset = (triplet << 2);
        char c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = (byte) c;
        }
        c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = (byte) c;
        }
        // Last is required to be non-empty
        buffer[offset++] = (byte) LEADING_TRIPLETS[digitOffset];
        return offset;
    }
    
    private static int outputFullTriplet(int triplet, char[] buffer, int offset)
    {
        int digitOffset = (triplet << 2);
        buffer[offset++] = FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = FULL_TRIPLETS[digitOffset];
        return offset;
    }

    private static int outputFullTriplet(int triplet, byte[] buffer, int offset)
    {
        int digitOffset = (triplet << 2);
        buffer[offset++] = FULL_TRIPLETS_B[digitOffset++];
        buffer[offset++] = FULL_TRIPLETS_B[digitOffset++];
        buffer[offset++] = FULL_TRIPLETS_B[digitOffset];
        return offset;
    }
    
    /**
     *<p>
     * Pre-conditions: posValue is positive, and larger than
     * Integer.MAX_VALUE (about 2 billions).
     */
    private static int calcLongStrLength(long posValue)
    {
        int len = 10;
        long comp = TEN_BILLION_L;

        // 19 is longest, need to worry about overflow
        while (posValue >= comp) {
            if (len == 19) {
                break;
            }
            ++len;
            comp = (comp << 3) + (comp << 1); // 10x
        }
        return len;
    }
}
