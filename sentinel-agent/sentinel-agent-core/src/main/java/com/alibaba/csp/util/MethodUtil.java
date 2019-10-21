package com.alibaba.csp.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MethodUtil {
   private static final Map<Method, String> methodNameMap = new ConcurrentHashMap();
   private static final Object LOCK = new Object();

   public static String resolveMethodName(Method method) {
      if (method == null) {
         throw new IllegalArgumentException("Null method");
      } else {
         String methodName = (String)methodNameMap.get(method);
         if (methodName == null) {
            synchronized(LOCK) {
               methodName = (String)methodNameMap.get(method);
               if (methodName == null) {
                  StringBuilder sb = new StringBuilder();
                  String className = method.getDeclaringClass().getName();
                  String name = method.getName();
                  Class<?>[] params = method.getParameterTypes();
                  sb.append(className).append(":").append(name);
                  sb.append("(");
                  int paramPos = 0;
                  Class[] var8 = params;
                  int var9 = params.length;

                  for(int var10 = 0; var10 < var9; ++var10) {
                     Class<?> clazz = var8[var10];
                     sb.append(clazz.getCanonicalName());
                     ++paramPos;
                     if (paramPos < params.length) {
                        sb.append(",");
                     }
                  }

                  sb.append(")");
                  methodName = sb.toString();
                  methodNameMap.put(method, methodName);
               }
            }
         }

         return methodName;
      }
   }

   static void clearMethodMap() {
      methodNameMap.clear();
   }
}
