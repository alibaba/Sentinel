package com.alibaba.acm.shaded.com.aliyuncs.utils;

import java.util.*;

public class FlattenMapUtil {
    public static List<Map<Object, Object>> toListMap(Map<String, String> flattenMap, String prefix) {

        Object object = null;
        String[] subKeys;
        String[] subPrefixes = prefix.split("\\.");
        for (Map.Entry<String, String> entry: flattenMap.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(prefix + "[")) {
                continue;
            }
            if (key.endsWith(".Length")) {
                continue;
            }
            subKeys = key.split("\\.");

            object = put(flattenMap, object, subKeys, subPrefixes.length - 1);
        }

        return (List<Map<Object, Object>>)object;
    }

    public static Map<Object, Object> toMap(Map<String, String> flattenMap, String prefix) {

        Object object = null;
        String[] subKeys;
        String[] subPrefixes = prefix.split("\\.");
        for (Map.Entry<String, String> entry: flattenMap.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(prefix + ".")) {
                continue;
            }
            if (key.endsWith(".Length")) {
                continue;
            }
            subKeys = key.split("\\.");

            object = put(flattenMap, object, subKeys, subPrefixes.length - 1);
        }

        return (Map<Object, Object>)object;
    }

    public static Object put(Map<String, String> flattenMap, Object object, String[] subKeys, int subKeysIndex) {
        if (subKeysIndex >= subKeys.length) {
            return object;
        }
        String key = subKeys[subKeysIndex];

        if (key.endsWith("]")) {
            int index = parseIndex(key);
            if (index == -1) {
                return null;
            }

            ArrayList<Object> arrayList;

            if (object == null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < subKeysIndex; i ++) {
                    sb.append(subKeys[i]).append(".");
                }
                sb.append(key);
                int length = parseLength(flattenMap, sb.toString());
                if (length == -1) {
                    return null;
                }
                arrayList = new ArrayList<Object>(Collections.nCopies(length,null));
            } else {
                arrayList = (ArrayList<Object>)object;
            }

            if (subKeys.length == subKeysIndex + 1) {
                arrayList.set(index, flattenMap.get(stringJoin(".", subKeys)));
                return arrayList;
            } else {
                arrayList.set(index, put(flattenMap, arrayList.get(index), subKeys, subKeysIndex + 1));
                return arrayList;
            }

        } else {
            HashMap<Object, Object> hashMap;
            if (object == null) {
                hashMap = new HashMap<Object, Object>();
            } else {
                hashMap = (HashMap<Object, Object>)object;
            }
            if (subKeys.length == subKeysIndex + 1) {
                hashMap.put(key, flattenMap.get(stringJoin(".", subKeys)));
                return hashMap;
            } else {
                hashMap.put(key, put(flattenMap, hashMap.get(key), subKeys, subKeysIndex + 1));
                return hashMap;
            }
        }
    }

    public static int parseIndex(String key) {
        int start = key.indexOf("[");
        int end = key.indexOf("]");
        if (start == -1 || end == -1 || end <= start) {
            return -1;
        }

        try {
            return Integer.parseInt(key.substring(start + 1, end));
        } catch (Exception e) {
            return -1;
        }
    }

    public static int parseLength(Map<String, String> flattenMap, String key) {
        int end = key.lastIndexOf("[");
        if (end == -1) {
            return -1;
        }

        try {
            return Integer.parseInt(flattenMap.get(key.substring(0, end) + ".Length"));
        } catch (Exception e) {
            return -1;
        }
    }

    public static String stringJoin(String delimiter, String ...sequences) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < sequences.length; i ++) {
            sb.append(sequences[i]);
            if (i < sequences.length - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }
}
