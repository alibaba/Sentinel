package com.alibaba.acm.shaded.org.codehaus.jackson.util;

import java.util.Arrays;

import com.alibaba.acm.shaded.org.codehaus.jackson.io.CharacterEscapes;

public final class CharTypes
{
    private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private final static byte[] HEX_BYTES;
    static {
        int len = HEX_CHARS.length;
        HEX_BYTES = new byte[len];
        for (int i = 0; i < len; ++i) {
            HEX_BYTES[i] = (byte) HEX_CHARS[i];
        }
    }


    /**
     * Lookup table used for determining which input characters
     * need special handling when contained in text segment.
     */
    final static int[] sInputCodes;
    static {
        /* 96 would do for most cases (backslash is ascii 94)
         * but if we want to do lookups by raw bytes it's better
         * to have full table
         */
        int[] table = new int[256];
        // Control chars and non-space white space are not allowed unquoted
        for (int i = 0; i < 32; ++i) {
            table[i] = -1;
        }
        // And then string end and quote markers are special too
        table['"'] = 1;
        table['\\'] = 1;
        sInputCodes = table;
    }

    /**
     * Additionally we can combine UTF-8 decoding info into similar
     * data table.
     */
    final static int[] sInputCodesUtf8;
    static {
        int[] table = new int[sInputCodes.length];
        System.arraycopy(sInputCodes, 0, table, 0, sInputCodes.length);
        for (int c = 128; c < 256; ++c) {
            int code;

            // We'll add number of bytes needed for decoding
            if ((c & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                code = 2;
            } else if ((c & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                code = 3;
            } else if ((c & 0xF8) == 0xF0) {
                // 4 bytes; double-char with surrogates and all...
                code = 4;
            } else {
                // And -1 seems like a good "universal" error marker...
                code = -1;
            }
            table[c] = code;
        }
        sInputCodesUtf8 = table;
    }

    /**
     * To support non-default (and -standard) unquoted field names mode,
     * need to have alternate checking.
     * Basically this is list of 8-bit ASCII characters that are legal
     * as part of Javascript identifier
     *
     * @since 1.2
     */
    final static int[] sInputCodesJsNames;
    static {
        int[] table = new int[256];
        // Default is "not a name char", mark ones that are
        Arrays.fill(table, -1);
        // Assume rules with JS same as Java (change if/as needed)
        for (int i = 33; i < 256; ++i) {
            if (Character.isJavaIdentifierPart((char) i)) {
                table[i] = 0;
            }
        }
        /* As per [JACKSON-267], '@', '#' and '*' are also to be accepted as well.
         * And '-' (for hyphenated names); and '+' for sake of symmetricity...
         */
        table['@'] = 0;
        table['#'] = 0;
        table['*'] = 0;
        table['-'] = 0;
        table['+'] = 0;
        sInputCodesJsNames = table;
    }

    /**
     * This table is similar to Latin-1, except that it marks all "high-bit"
     * code as ok. They will be validated at a later point, when decoding
     * name
     */
    final static int[] sInputCodesUtf8JsNames;
    static {
        int[] table = new int[256];
        // start with 8-bit JS names 
        System.arraycopy(sInputCodesJsNames, 0, table, 0, sInputCodesJsNames.length);
        Arrays.fill(table, 128, 128, 0);
        sInputCodesUtf8JsNames = table;
    }

    /**
     * Decoding table used to quickly determine characters that are
     * relevant within comment content
     */
    final static int[] sInputCodesComment = new int[256];
    static {
        // but first: let's start with UTF-8 multi-byte markers:
        System.arraycopy(sInputCodesUtf8, 128, sInputCodesComment, 128, 128);
    
        // default (0) means "ok" (skip); -1 invalid, others marked by char itself
        Arrays.fill(sInputCodesComment, 0, 32, -1); // invalid white space
        sInputCodesComment['\t'] = 0; // tab is still fine
        sInputCodesComment['\n'] = '\n'; // lf/cr need to be observed, ends cpp comment
        sInputCodesComment['\r'] = '\r';
        sInputCodesComment['*'] = '*'; // end marker for c-style comments
    }

    /**
     * Lookup table used for determining which output characters in 
     * 7-bit ASCII range need to be quoted.
     */
    final static int[] sOutputEscapes128;
    static {
        int[] table = new int[128];
        // Control chars need generic escape sequence
        for (int i = 0; i < 32; ++i) {
            // 04-Mar-2011, tatu: Used to use "-(i + 1)", replaced with constants
            table[i] = CharacterEscapes.ESCAPE_STANDARD;
        }
        /* Others (and some within that range too) have explicit shorter
         * sequences
         */
        table['"'] = '"';
        table['\\'] = '\\';
        // Escaping of slash is optional, so let's not add it
        table[0x08] = 'b';
        table[0x09] = 't';
        table[0x0C] = 'f';
        table[0x0A] = 'n';
        table[0x0D] = 'r';
        sOutputEscapes128 = table;
    }

    /**
     * Lookup table for the first 128 Unicode characters (7-bit ASCII)
     * range. For actual hex digits, contains corresponding value;
     * for others -1.
     */
    final static int[] sHexValues = new int[128];
    static {
        Arrays.fill(sHexValues, -1);
        for (int i = 0; i < 10; ++i) {
            sHexValues['0' + i] = i;
        }
        for (int i = 0; i < 6; ++i) {
            sHexValues['a' + i] = 10 + i;
            sHexValues['A' + i] = 10 + i;
        }
    }

    public final static int[] getInputCodeLatin1() { return sInputCodes; }
    public final static int[] getInputCodeUtf8() { return sInputCodesUtf8; }

    public final static int[] getInputCodeLatin1JsNames() { return sInputCodesJsNames; }
    public final static int[] getInputCodeUtf8JsNames() { return sInputCodesUtf8JsNames; }

    public final static int[] getInputCodeComment() { return sInputCodesComment; }
    
    /**
     * Accessor for getting a read-only encoding table for first 128 Unicode
     * code points (single-byte UTF-8 characters).
     * Value of 0 means "no escaping"; other positive values that value is character
     * to use after backslash; and negative values that generic (backslash - u)
     * escaping is to be used.
     */
    public final static int[] get7BitOutputEscapes() { return sOutputEscapes128; }

    public static int charToHex(int ch)
    {
        return (ch > 127) ? -1 : sHexValues[ch];
    }

    public static void appendQuoted(StringBuilder sb, String content)
    {
        final int[] escCodes = sOutputEscapes128;
        int escLen = escCodes.length;
        for (int i = 0, len = content.length(); i < len; ++i) {
            char c = content.charAt(i);
            if (c >= escLen || escCodes[c] == 0) {
                sb.append(c);
                continue;
            }
            sb.append('\\');
            int escCode = escCodes[c];
            if (escCode < 0) { // generic quoting (hex value)
                // We know that it has to fit in just 2 hex chars
                sb.append('u');
                sb.append('0');
                sb.append('0');
                int value = -(escCode + 1);
                sb.append(HEX_CHARS[value >> 4]);
                sb.append(HEX_CHARS[value & 0xF]);
            } else { // "named", i.e. prepend with slash
                sb.append((char) escCode);
            }
        }
    }

    /**
     * @since 1.6
     */
    public static char[] copyHexChars()
    {
        return (char[]) HEX_CHARS.clone();
    }

    /**
     * @since 1.6
     */
    public static byte[] copyHexBytes()
    {
        return (byte[]) HEX_BYTES.clone();
    }
}

