package com.taobao.csp.ahas.gw.utils.ds;

import java.util.Map;

/** @deprecated */
@Deprecated
public final class MapUtil {
   public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
      V v = map.get(key);
      if (v == null) {
         v = map.put(key, value);
      }

      return v;
   }
}
