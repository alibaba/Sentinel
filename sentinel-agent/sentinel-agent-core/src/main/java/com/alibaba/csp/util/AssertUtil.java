package com.alibaba.csp.util;

public class AssertUtil {
   private AssertUtil() {
   }

   public static void notEmpty(String string, String message) {
      if (StringUtil.isEmpty(string)) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void assertNotBlank(String string, String message) {
      if (StringUtil.isBlank(string)) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void notNull(Object object, String message) {
      if (object == null) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void isTrue(boolean value, String message) {
      if (!value) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void assertState(boolean condition, String message) {
      if (!condition) {
         throw new IllegalStateException(message);
      }
   }
}
