package com.alibaba.acm.shaded.org.json;

import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/**
 * A JSON Pointer is a simple query language defined for JSON documents by
 * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
 * 
 * In a nutshell, JSONPointer allows the user to navigate into a JSON document
 * using strings, and retrieve targeted objects, like a simple form of XPATH.
 * Path segments are separated by the '/' char, which signifies the root of
 * the document when it appears as the first char of the string. Array 
 * elements are navigated using ordinals, counting from 0. JSONPointer strings
 * may be extended to any arbitrary number of segments. If the navigation
 * is successful, the matched item is returned. A matched item may be a
 * JSONObject, a JSONArray, or a JSON value. If the JSONPointer string building 
 * fails, an appropriate exception is thrown. If the navigation fails to find
 * a match, a JSONPointerException is thrown. 
 * 
 * @author JSON.org
 * @version 2016-05-14
 */
public class JSONPointer {

    // used for URL encoding and decoding
    private static final String ENCODING = "utf-8";

    /**
     * This class allows the user to build a JSONPointer in steps, using
     * exactly one segment in each step.
     */
    public static class Builder {

        // Segments for the eventual JSONPointer string
        private final List<String> refTokens = new ArrayList<String>();

        /**
         * Creates a {@code JSONPointer} instance using the tokens previously set using the
         * {@link #append(String)} method calls.
         */
        public JSONPointer build() {
            return new JSONPointer(refTokens);
        }

        /**
         * Adds an arbitary token to the list of reference tokens. It can be any non-null value.
         * 
         * Unlike in the case of JSON string or URI fragment representation of JSON pointers, the
         * argument of this method MUST NOT be escaped. If you want to query the property called
         * {@code "a~b"} then you should simply pass the {@code "a~b"} string as-is, there is no
         * need to escape it as {@code "a~0b"}.
         * 
         * @param token the new token to be appended to the list
         * @return {@code this}
         * @throws NullPointerException if {@code token} is null
         */
        public Builder append(String token) {
            if (token == null) {
                throw new NullPointerException("token cannot be null");
            }
            refTokens.add(token);
            return this;
        }

        /**
         * Adds an integer to the reference token list. Although not necessarily, mostly this token will
         * denote an array index. 
         * 
         * @param arrayIndex the array index to be added to the token list
         * @return {@code this}
         */
        public Builder append(int arrayIndex) {
            refTokens.add(String.valueOf(arrayIndex));
            return this;
        }
    }

    /**
     * Static factory method for {@link Builder}. Example usage:
     * 
     * <pre><code>
     * JSONPointer pointer = JSONPointer.builder()
     *       .append("obj")
     *       .append("other~key").append("another/key")
     *       .append("\"")
     *       .append(0)
     *       .build();
     * </code></pre>
     * 
     *  @return a builder instance which can be used to construct a {@code JSONPointer} instance by chained
     *  {@link Builder#append(String)} calls.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Segments for the JSONPointer string
    private final List<String> refTokens;

    /**
     * Pre-parses and initializes a new {@code JSONPointer} instance. If you want to
     * evaluate the same JSON Pointer on different JSON documents then it is recommended
     * to keep the {@code JSONPointer} instances due to performance considerations.
     * 
     * @param pointer the JSON String or URI Fragment representation of the JSON pointer.
     * @throws IllegalArgumentException if {@code pointer} is not a valid JSON pointer
     */
    public JSONPointer(String pointer) {
        if (pointer == null) {
            throw new NullPointerException("pointer cannot be null");
        }
        if (pointer.isEmpty() || pointer.equals("#")) {
            refTokens = Collections.emptyList();
            return;
        }
        if (pointer.startsWith("#/")) {
            pointer = pointer.substring(2);
            try {
                pointer = URLDecoder.decode(pointer, ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else if (pointer.startsWith("/")) {
            pointer = pointer.substring(1);
        } else {
            throw new IllegalArgumentException("a JSON pointer should start with '/' or '#/'");
        }
        refTokens = new ArrayList<String>();
        for (String token : pointer.split("/")) {
            refTokens.add(unescape(token));
        }
    }

    public JSONPointer(List<String> refTokens) {
        this.refTokens = new ArrayList<String>(refTokens);
    }

    private String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * Evaluates this JSON Pointer on the given {@code document}. The {@code document}
     * is usually a {@link JSONObject} or a {@link JSONArray} instance, but the empty
     * JSON Pointer ({@code ""}) can be evaluated on any JSON values and in such case the
     * returned value will be {@code document} itself. 
     * 
     * @param document the JSON document which should be the subject of querying.
     * @return the result of the evaluation
     * @throws JSONPointerException if an error occurs during evaluation
     */
    public Object queryFrom(Object document) {
        if (refTokens.isEmpty()) {
            return document;
        }
        Object current = document;
        for (String token : refTokens) {
            if (current instanceof JSONObject) {
                current = ((JSONObject) current).opt(unescape(token));
            } else if (current instanceof JSONArray) {
                current = readByIndexToken(current, token);
            } else {
                throw new JSONPointerException(format(
                        "value [%s] is not an array or object therefore its key %s cannot be resolved", current,
                        token));
            }
        }
        return current;
    }

    /**
     * Matches a JSONArray element by ordinal position
     * @param current the JSONArray to be evaluated
     * @param indexToken the array index in string form
     * @return the matched object. If no matching item is found a
     * JSONPointerException is thrown
     */
    private Object readByIndexToken(Object current, String indexToken) {
        try {
            int index = Integer.parseInt(indexToken);
            JSONArray currentArr = (JSONArray) current;
            if (index >= currentArr.length()) {
                throw new JSONPointerException(format("index %d is out of bounds - the array has %d elements", index,
                        currentArr.length()));
            }
            return currentArr.get(index);
        } catch (NumberFormatException e) {
            throw new JSONPointerException(format("%s is not an array index", indexToken), e);
        }
    }

    /**
     * Returns a string representing the JSONPointer path value using string
     * representation
     */
    @Override
    public String toString() {
        StringBuilder rval = new StringBuilder("");
        for (String token: refTokens) {
            rval.append('/').append(escape(token));
        }
        return rval.toString();
    }

    /**
     * Escapes path segment values to an unambiguous form.
     * The escape char to be inserted is '~'. The chars to be escaped 
     * are ~, which maps to ~0, and /, which maps to ~1. Backslashes
     * and double quote chars are also escaped.
     * @param token the JSONPointer segment value to be escaped
     * @return the escaped value for the token
     */
    private String escape(String token) {
        return token.replace("~", "~0")
                .replace("/", "~1")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Returns a string representing the JSONPointer path value using URI
     * fragment identifier representation
     */
    public String toURIFragment() {
        try {
            StringBuilder rval = new StringBuilder("#");
            for (String token : refTokens) {
                rval.append('/').append(URLEncoder.encode(token, ENCODING));
            }
            return rval.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
