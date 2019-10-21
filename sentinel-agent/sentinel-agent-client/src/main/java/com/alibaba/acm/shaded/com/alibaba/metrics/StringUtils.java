package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.util.regex.Pattern;

/**
 * @author wangtao 2016-10-13 16:14.
 */
public class StringUtils {

    /**
     * According to Opentsdb specification, only the following characters are allowed:
     * a to z, A to Z, 0 to 9, -, _, ., / or Unicode letters (as per the specification)
     */
    private static final String ILLEGAL_CHARS = "[^a-zA-Z0-9-_./\\p{L}]";

    /**
     * \p{L} means to match the unicode letters
     * see http://www.regular-expressions.info/unicode.html#prop
     */
    private static final Pattern ILLEGAL_CHARS_PATTERN = Pattern.compile(ILLEGAL_CHARS);

    /**
     * check if there is any illegal character in given metric name
     * Focus on metric key and tag value
     * @param name the metric name to check
     */
    public static void checkMetricName(MetricName name) {
        if (name == null) return;

        if (ILLEGAL_CHARS_PATTERN.matcher(name.getKey()).find()) {
            throw new IllegalArgumentException("Illegal metric key: " + name.getKey());
        }

        for (String tagV: name.getTags().values()) {
            checkTagValue(tagV);
        }
    }


    /**
     * check if there is any illegal character in tag value
     * @param tagValue the tag value to check
     */
    public static void checkTagValue(String tagValue) {
        if (tagValue == null) return;

        if (ILLEGAL_CHARS_PATTERN.matcher(tagValue).find()) {
            throw new IllegalArgumentException("Illegal tag value: " + tagValue);
        }
    }

    /**
     * This is no longer an issue to Hitsdb.
     * When output to metrics.log, all the illegal chars will be replaced by "_"
     * @param input the input string
     * @return a string with all the illegal chars replaced by "_"
     */
    @Deprecated
    public static String removeIllegalOpentsdbChars(String input) {
        if (input == null) return null;
        return input.replaceAll(ILLEGAL_CHARS, "_");
    }

    public static boolean equals(String s1, String s2) {

        if (s1 == null && s2 == null) {
            return true;
        }

        if (s1 == null || s2 == null) {
            return false;
        }

        return s1.equals(s2);
    }

    public static boolean isNotBlank(String s) {

        if (s != null && !"".equals(s)) {
            return true;
        }

        return false;
    }

    public static boolean isBlank(String s) {

        if (s == null || "".equals(s)) {
            return true;
        }

        return false;
    }
    
}
