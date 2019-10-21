/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.annotation.adapters;



/**
 * {@link XmlAdapter} to handle <tt>xs:normalizedString</tt>.
 *
 * <p>
 * This adapter removes leading and trailing whitespaces, then replace
 * any tab, CR, and LF by a whitespace character ' '.
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB 2.0
 */
public final class NormalizedStringAdapter extends XmlAdapter<String,String> {
    /**
     * Replace any tab, CR, and LF by a whitespace character ' ',
     * as specified in <a href="http://www.w3.org/TR/xmlschema-2/#rf-whiteSpace">the whitespace facet 'replace'</a>
     */
    public String unmarshal(String text) {
        if(text==null)      return null;    // be defensive

        int i=text.length()-1;

        // look for the first whitespace char.
        while( i>=0 && !isWhiteSpaceExceptSpace(text.charAt(i)) )
            i--;

        if( i<0 )
            // no such whitespace. replace(text)==text.
            return text;

        // we now know that we need to modify the text.
        // allocate a char array to do it.
        char[] buf = text.toCharArray();

        buf[i--] = ' ';
        for( ; i>=0; i-- )
            if( isWhiteSpaceExceptSpace(buf[i]))
                buf[i] = ' ';

        return new String(buf);
    }

    /**
     * No-op.
     *
     * Just return the same string given as the parameter.
     */
        public String marshal(String s) {
            return s;
        }


    /**
     * Returns true if the specified char is a white space character
     * but not 0x20.
     */
    protected static boolean isWhiteSpaceExceptSpace(char ch) {
        // most of the characters are non-control characters.
        // so check that first to quickly return false for most of the cases.
        if( ch>=0x20 )   return false;

        // other than we have to do four comparisons.
        return ch == 0x9 || ch == 0xA || ch == 0xD;
    }
}
