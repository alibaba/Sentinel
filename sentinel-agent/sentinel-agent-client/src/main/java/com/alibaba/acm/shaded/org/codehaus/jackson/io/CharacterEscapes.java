package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import com.alibaba.acm.shaded.org.codehaus.jackson.SerializableString;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.CharTypes;

/**
 * Abstract base class that defines interface for customizing character
 * escaping aspects for String values, for formats that use escaping.
 * For JSON this applies to both property names and String values.
 *
 * @since 1.8
 */
public abstract class CharacterEscapes
{
    /**
     * Value used for lookup tables to indicate that matching characters
     * do not need to be escaped.
     */
    public final static int ESCAPE_NONE = 0;

    /**
     * Value used for lookup tables to indicate that matching characters
     * are to be escaped using standard escaping; for JSON this means
     * (for example) using "backslash - u" escape method.
     */
    public final static int ESCAPE_STANDARD = -1;

    /**
     * Value used for lookup tables to indicate that matching characters
     * will need custom escapes; and that another call
     * to {@link #getEscapeSequence} is needed to figure out exact escape
     * sequence to output.
     */
    public final static int ESCAPE_CUSTOM = -2;
    
    /**
     * Method generators can call to get lookup table for determining
     * escape handling for first 128 characters of Unicode (ASCII
     * characters. Caller is not to modify contents of this array, since
     * this is expected to be a shared copy.
     * 
     * @return Array with size of at least 128, where first 128 entries
     *    have either one of <code>ESCAPE_xxx</code> constants, or non-zero positive
     *    integer (meaning of which is data format specific; for JSON it means
     *    that combination of backslash and character with that value is to be used)
     *    to indicate that specific escape sequence is to be used.
     */
    public abstract int[] getEscapeCodesForAscii();

    /**
     * Method generators can call to get lookup table for determining
     * exact escape sequence to use for given character.
     * It can be called for any character, but typically is called for
     * either for ASCII characters for which custom escape
     * sequence is needed; or for any non-ASCII character.
     */
    public abstract SerializableString getEscapeSequence(int ch);

    /**
     * Helper method that can be used to get a copy of standard JSON
     * escape definitions; this is useful when just wanting to slightly
     * customize definitions. Caller can modify this array as it sees
     * fit and usually returns modified instance via {@link #getEscapeCodesForAscii}
     */
    public static int[] standardAsciiEscapesForJSON()
    {
        int[] esc = CharTypes.get7BitOutputEscapes();
        int len = esc.length;
        int[] result = new int[len];
        System.arraycopy(esc, 0, result, 0, esc.length);
        return result;
    }
}
