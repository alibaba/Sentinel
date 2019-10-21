package com.alibaba.jvm.sandbox.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Feature编解器(线程安全)
 * Created by luanjia@taobao.com on 16/8/24.
 */
public class FeatureCodec {

    /**
     * KV片段分割符
     * KV片段定义为一个完整的KV对，例如字符串<span>;k1=v1;k2=v2;</span>
     * 其中<b>;</b>即为KV片段分隔符
     */
    private final char kvSegmentSeparator;

    /**
     * KV分割符
     * KV定义为一个KV对区分K和V的分割符号，例如字符串<span>k1=v1</span>
     * 其中<b>=</b>即为KV分隔符
     */
    private final char kvSeparator;

    /**
     * 转义前缀符
     */
    private final char ESCAPE_PREFIX_CHAR = '\\';

    /**
     * 使用指定的KV分割符构造FeatureParser
     *
     * @param kvSegmentSeparator KV对之间的分隔符
     * @param kvSeparator        K与V之间的分隔符
     */
    public FeatureCodec(final char kvSegmentSeparator, final char kvSeparator) {

        // 分隔符禁止与转义前缀符相等
        if (ESCAPE_PREFIX_CHAR == kvSegmentSeparator
                || ESCAPE_PREFIX_CHAR == kvSeparator) {
            throw new IllegalArgumentException("separator can not equals '" + ESCAPE_PREFIX_CHAR + "'.");
        }

        this.kvSegmentSeparator = kvSegmentSeparator;
        this.kvSeparator = kvSeparator;
    }


    /**
     * collection集合转换到feature字符串
     *
     * @param collection 待转换的collection集合
     * @return feature字符串
     */
    public String toString(final Collection<String> collection) {
        final StringBuilder featureSB = new StringBuilder().append(kvSegmentSeparator);
        if (null == collection
                || collection.isEmpty()) {
            return featureSB.toString();
        }

        for (String value : collection) {
            if (StringUtils.isNotBlank(value)) {
                featureSB
                        .append(escapeEncode(value))
                        .append(kvSegmentSeparator)
                ;
            }
        }
        return featureSB.toString();
    }

    /**
     * feature字符串转换到collection集合
     *
     * @param featureString the feature string
     * @return the collection
     */
    public Collection<String> toCollection(final String featureString) {

        final Collection<String> collection = new ArrayList<String>();

        if (StringUtils.isBlank(featureString)) {
            return collection;
        }

        for (String value : escapeSplit(featureString, kvSegmentSeparator)) {

            if (StringUtils.isBlank(value)) {
                // 过滤掉为空的字符串片段
                continue;
            }

            collection.add(escapeDecode(value));

        }

        return collection;
    }


    /**
     * map集合转换到feature字符串
     *
     * @param map map集合
     * @return feature字符串
     */
    public String toString(final Map<String, String> map) {

        final StringBuilder featureSB = new StringBuilder().append(kvSegmentSeparator);
        if (null == map
                || map.isEmpty()) {
            return featureSB.toString();
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (StringUtils.isNotBlank(key)
                    && StringUtils.isNotBlank(value)) {
                featureSB
                        .append(escapeEncode(key))
                        .append(kvSeparator)
                        .append(escapeEncode(value))
                        .append(kvSegmentSeparator)
                ;
            }
        }

        return featureSB.toString();
    }


    /**
     * feature字符串转换到map集合
     *
     * @param featureString the feature string
     * @return the map
     */
    public Map<String, String> toMap(final String featureString) {

        final Map<String, String> map = new HashMap<String, String>();

        if (StringUtils.isBlank(featureString)) {
            return map;
        }

        for (String kv : escapeSplit(featureString, kvSegmentSeparator)) {

            if (StringUtils.isBlank(kv)) {
                // 过滤掉为空的字符串片段
                continue;
            }

            final String[] ar = escapeSplit(kv, kvSeparator);
            if (ar.length != 2) {
                // 过滤掉不符合K:V单目的情况
                continue;
            }

            final String k = ar[0];
            final String v = ar[1];
            if (StringUtils.isNotBlank(k)
                    && StringUtils.isNotBlank(v)) {
                map.put(escapeDecode(k), escapeDecode(v));
            }

        }

        return map;
    }


    /**
     * 是否特殊字符
     * 特殊字符是指需要被转义的，用于features内部字段控制的字符
     */
    private boolean isSpecialChar(final char c) {
        return c == kvSegmentSeparator
                || c == kvSeparator
                || c == ESCAPE_PREFIX_CHAR;
    }

    /**
     * 转义编码
     *
     * @param string 原始字符串
     * @return 转义编码后的字符串
     */
    private String escapeEncode(final String string) {
        final int len = string.length();
        final char[] charArray = new char[len * 2];
        int charArrayIndex = 0;

        for (int index = 0; index < len; index++) {
            final char c = string.charAt(index);
            if (isSpecialChar(c)) {
                charArray[charArrayIndex++] = ESCAPE_PREFIX_CHAR;
            }
            charArray[charArrayIndex++] = c;
        }

        return new String(charArray, 0, charArrayIndex);
    }

    /**
     * 转义解码
     *
     * @param string 编码字符串
     * @return 转义解码后的字符串
     */
    private String escapeDecode(final String string) {

        final int len = string.length();
        final char[] segmentCharArray = new char[len];

        int segmentCharArrayIndex = 0;
        ESCAPE_DECODE_STATE state = ESCAPE_DECODE_STATE.READ_CHAR;

        for (int index = 0; index < len; index++) {

            final char c = string.charAt(index);
            switch (state) {
                case READ_CHAR: {
                    if (c == ESCAPE_PREFIX_CHAR) {
                        state = ESCAPE_DECODE_STATE.ESCAPE_CHAR;
                    } else {
                        segmentCharArray[segmentCharArrayIndex++] = c;
                    }
                    break;
                }

                case ESCAPE_CHAR: {
                    state = ESCAPE_DECODE_STATE.READ_CHAR;
                    if (isSpecialChar(c)) {
                        segmentCharArray[segmentCharArrayIndex++] = c;
                    } else {
                        segmentCharArray[segmentCharArrayIndex++] = ESCAPE_PREFIX_CHAR;
                        segmentCharArray[segmentCharArrayIndex++] = c;
                    }
                    break;
                }
            }//switch
        }//for

        return new String(segmentCharArray, 0, segmentCharArrayIndex);
    }

    private void addToArrayList(final ArrayList<String> segmentArrayList, final String segmentString) {
        segmentArrayList.add(
                segmentString
                        .trim() // 考虑到字符串片段可能会出现首尾空格的场景，这里做一个过滤
        );
    }

    /**
     * 编码字符串拆分
     *
     * @param string    编码字符串
     * @param splitChar 分割符
     * @return 拆分后的字符串数组
     */
    private String[] escapeSplit(final String string, final char splitChar) {

        final int len = string.length();
        final ArrayList<String> segmentArrayList = new ArrayList<String>(len);
        final char[] segmentCharArray = new char[len];

        int segmentCharArrayIndex = 0;
        ESCAPE_SPLIT_STATE state = ESCAPE_SPLIT_STATE.READ_CHAR;

        for (int index = 0; index < len; ) {

            final char c = string.charAt(index);
            switch (state) {

                case READ_CHAR: {

                    // 匹配到转义符
                    if (c == ESCAPE_PREFIX_CHAR) {
                        state = ESCAPE_SPLIT_STATE.ESCAPE_CHAR;
                        segmentCharArray[segmentCharArrayIndex++] = c;
                        break;
                    }

                    // 匹配到分隔符
                    else if (c == splitChar) {
                        state = ESCAPE_SPLIT_STATE.SPLIT_CHAR;
                        continue;
                    }

                    // 其他字符
                    else {
                        segmentCharArray[segmentCharArrayIndex++] = c;
                        break;
                    }

                }

                case SPLIT_CHAR: {
                    state = ESCAPE_SPLIT_STATE.READ_CHAR;
                    addToArrayList(segmentArrayList, new String(segmentCharArray, 0, segmentCharArrayIndex));
                    segmentCharArrayIndex = 0;
                    break;
                }

                case ESCAPE_CHAR: {
                    state = ESCAPE_SPLIT_STATE.READ_CHAR;
                    segmentCharArray[segmentCharArrayIndex++] = c;
                    break;
                }

            }//switch

            index++;

        }

        addToArrayList(segmentArrayList, new String(segmentCharArray, 0, segmentCharArrayIndex));
        return segmentArrayList.toArray(new String[0]);
    }

    /**
     * 转义解码状态枚举
     */
    private enum ESCAPE_DECODE_STATE {

        /**
         * 转义符
         */
        ESCAPE_CHAR,

        /**
         * 读取字符
         */
        READ_CHAR,

    }

    /**
     * 逃逸分割状态枚举
     */
    private enum ESCAPE_SPLIT_STATE {

        /**
         * 转义符
         */
        ESCAPE_CHAR,

        /**
         * 分割符
         */
        SPLIT_CHAR,

        /**
         * 读取字符
         */
        READ_CHAR,

    }

}
