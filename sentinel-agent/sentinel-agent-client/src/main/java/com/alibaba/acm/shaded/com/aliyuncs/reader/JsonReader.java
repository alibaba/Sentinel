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
package com.alibaba.acm.shaded.com.aliyuncs.reader;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class JsonReader implements Reader {

    private static final Object ARRAY_END_TOKEN = new Object();
    private static final Object OBJECT_END_TOKEN = new Object();
    private static final Object COMMA_TOKEN = new Object();
    private static final Object COLON_TOKEN = new Object();

    private static final int FIRST_POSITION = 0;
    private static final int CURRENT_POSITION = 1;
    private static final int NEXT_POSITION = 2;

    private CharacterIterator ct;
    private char c;
    private Object token;
    private StringBuffer stringBuffer = new StringBuffer();
    private Map<String, String> map = new HashMap<String, String>();

    private static Map<Character, Character> escapes = new HashMap<Character, Character>();

    static {
        escapes.put(Character.valueOf('\\'), Character.valueOf('\\'));
        escapes.put(Character.valueOf('/'), Character.valueOf('/'));
        escapes.put(Character.valueOf('"'), Character.valueOf('"'));
        escapes.put(Character.valueOf('t'), Character.valueOf('\t'));
        escapes.put(Character.valueOf('n'), Character.valueOf('\n'));
        escapes.put(Character.valueOf('r'), Character.valueOf('\r'));
        escapes.put(Character.valueOf('b'), Character.valueOf('\b'));
        escapes.put(Character.valueOf('f'), Character.valueOf('\f'));
    }

    @Override
    public Map<String, String> read(String response, String endpoint) {
        return read(new StringCharacterIterator(response), endpoint, FIRST_POSITION);
    }
    
    public Map<String, String> readForHideArrayItem(String response, String endpoint) {
        return readForHideItem(new StringCharacterIterator(response), endpoint, FIRST_POSITION);
    }

    public Map<String, String> read(CharacterIterator ci, String endpoint, int start) {
        ct = ci;
        switch (start) {
            case FIRST_POSITION:
                c = ct.first();
                break;
            case CURRENT_POSITION:
                c = ct.current();
                break;
            case NEXT_POSITION:
                c = ct.next();
                break;
            default:
                break;
        }
        readJson(endpoint);
        return map;
    }
    
    public Map<String, String> readForHideItem(CharacterIterator ci, String endpoint, int start) {
        ct = ci;
        switch (start) {
            case FIRST_POSITION:
                c = ct.first();
                break;
            case CURRENT_POSITION:
                c = ct.current();
                break;
            case NEXT_POSITION:
                c = ct.next();
                break;
        }
        readJsonForHideItem(endpoint);
        return map;
    }

    private Object readJson(String baseKey) {
        skipWhiteSpace();
        char ch = c;
        nextChar();
        switch (ch) {
            case '{':
                processObject(baseKey);
                break;
            case '}':
                token = OBJECT_END_TOKEN;
                break;
            case '[':
                if (c == '"') {
                    processList(baseKey);
                    break;
                } else {
                    processArray(baseKey);
                    break;
                }
            case ']':
                token = ARRAY_END_TOKEN;
                break;
            case '"':
                token = processString();
                break;
            case ',':
                token = COMMA_TOKEN;
                break;
            case ':':
                token = COLON_TOKEN;
                break;
            case 't':
                nextChar();
                nextChar();
                nextChar();
                token = Boolean.TRUE;
                break;
            case 'n':
                nextChar();
                nextChar();
                nextChar();
                token = null;
                break;
            case 'f':
                nextChar();
                nextChar();
                nextChar();
                nextChar();
                token = Boolean.FALSE;
                break;
            default:
                c = ct.previous();
                if (Character.isDigit(c) || c == '-') {
                    token = processNumber();
                }
        }
        return token;
    }
    
    private Object readJsonForHideItem(String baseKey) {
        skipWhiteSpace();
        char ch = c;
        nextChar();
        switch (ch) {
            case '{':
                processObjectForHideItemName(baseKey);
                break;
            case '}':
                token = OBJECT_END_TOKEN;
                break;
            case '[':
                if (c == '"') {
                    processListForHideItem(baseKey);
                    break;
                } else {
                    processArrayForHideItem(baseKey);
                    break;
                }
            case ']':
                token = ARRAY_END_TOKEN;
                break;
            case '"':
                token = processString();
                break;
            case ',':
                token = COMMA_TOKEN;
                break;
            case ':':
                token = COLON_TOKEN;
                break;
            case 't':
                nextChar();
                nextChar();
                nextChar();
                token = Boolean.TRUE;
                break;
            case 'n':
                nextChar();
                nextChar();
                nextChar();
                token = null;
                break;
            case 'f':
                nextChar();
                nextChar();
                nextChar();
                nextChar();
                token = Boolean.FALSE;
                break;
            default:
                c = ct.previous();
                if (Character.isDigit(c) || c == '-') {
                    token = processNumber();
                }
        }
        return token;
    }

    private void processObject(String baseKey) {
        String key = baseKey + "." + readJson(baseKey);
        while (token != OBJECT_END_TOKEN) {
            readJson(key);
            if (token != OBJECT_END_TOKEN) {
                Object object = readJson(key);
                if (object instanceof String || object instanceof Number || object instanceof Boolean) {
                    map.put(key, String.valueOf(object));
                }

                if (readJson(key) == COMMA_TOKEN) {
                    key = String.valueOf(readJson(key));
                    key = baseKey + "." + key;
                }
            }
        }
    }
    
    private void processObjectForHideItemName(String baseKey) {
        String key = baseKey + "." + readJsonForHideItem(baseKey);
        while (token != OBJECT_END_TOKEN) {
            readJsonForHideItem(key);
            if (token != OBJECT_END_TOKEN) {
                Object object = readJsonForHideItem(key);
                if (object instanceof String || object instanceof Number || object instanceof Boolean) {
                    map.put(key, String.valueOf(object));
                }

                if (readJson(key) == COMMA_TOKEN) {
                    key = String.valueOf(readJson(key));
                    key = baseKey + "." + key;
                }
            }
        }
    }

    private void processList(String baseKey) {
        Object value = readJson(baseKey);
        int index = 0;
        while (token != ARRAY_END_TOKEN) {
            String key = trimFromLast(baseKey, ".") + "[" + (index++) + "]";
            map.put(key, String.valueOf(value));
            if (readJson(baseKey) == COMMA_TOKEN) {
                value = readJson(baseKey);
            }
        }
        map.put(trimFromLast(baseKey, ".") + ".Length", String.valueOf(index));
    }

    private void processListForHideItem(String baseKey) {
        Object value = readJson(baseKey);
        int index = 0;
        while (token != ARRAY_END_TOKEN) {
            String key = baseKey + "[" + (index++) + "]";
            map.put(key, String.valueOf(value));
            if (readJson(baseKey) == COMMA_TOKEN) {
                value = readJson(baseKey);
            }
        }
        map.put(baseKey + ".Length", String.valueOf(index));
    }

    private void processArray(String baseKey) {
        int index = 0;
        String preKey = baseKey.substring(0, baseKey.lastIndexOf("."));
        String key = preKey + "[" + index + "]";
        Object value = readJson(key);

        while (token != ARRAY_END_TOKEN) {
            map.put(preKey + ".Length", String.valueOf(index + 1));
            if (value instanceof String) {
                map.put(key, String.valueOf(value));
            }
            if (readJson(baseKey) == COMMA_TOKEN) {
                key = preKey + "[" + (++index) + "]";
                value = readJson(key);
            }
        }
    }
    
    private void processArrayForHideItem(String baseKey) {
        int index = 0;
        String preKey = baseKey;
        String key = preKey + "[" + index + "]";
        Object value = readJson(key);

        while (token != ARRAY_END_TOKEN) {
            map.put(preKey + ".Length", String.valueOf(index + 1));
            if (value instanceof String) {
                map.put(key, String.valueOf(value));
            }
            if (readJson(baseKey) == COMMA_TOKEN) {
                key = preKey + "[" + (++index) + "]";
                value = readJson(key);
            }
        }
    }

    private Object processNumber() {
        stringBuffer.setLength(0);
        if ('-' == c) {
            addChar();
        }
        addDigits();
        if ('.' == c) {
            addChar();
            addDigits();
        }
        if ('e' == c || 'E' == c) {
            addChar();
            if ('+' == c || '-' == c) {
                addChar();
            }
            addDigits();
        }
        return stringBuffer.toString();
    }

    private void addDigits() {
        while (Character.isDigit(c)) {
            addChar();
        }
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(c)) {
            nextChar();
        }
    }

    private char nextChar() {
        c = ct.next();
        return c;
    }

    private Object processString() {
        stringBuffer.setLength(0);
        while (c != '"') {
            if (c == '\\') {
                nextChar();
                Object value = escapes.get(Character.valueOf(c));
                if (value != null) {
                    addChar(((Character)value).charValue());
                }
            } else {
                addChar();
            }
        }
        nextChar();
        return stringBuffer.toString();
    }

    private void addChar(char ch) {
        stringBuffer.append(ch);
        nextChar();
    }

    private void addChar() {
        addChar(c);
    }

    public static String trimFromLast(String str, String stripString) {
        int pos = str.lastIndexOf(stripString);
        if (pos > -1) {
            return str.substring(0, pos);
        } else {
            return str;
        }
    }

}
