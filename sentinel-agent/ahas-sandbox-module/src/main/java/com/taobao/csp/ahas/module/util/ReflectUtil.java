package com.taobao.csp.ahas.module.util;

import com.taobao.csp.third.com.alibaba.fastjson.util.ASMUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {
   public static <T> T invokeMethod(Object obj, String methodName, Object[] args, boolean throwException) throws Exception {
      return obj == null ? null : invoke(obj.getClass(), obj, methodName, args, throwException);
   }

   public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Object[] args, boolean throwException) throws Exception {
      return invoke(clazz, (Object)null, methodName, args, throwException);
   }

   private static <T> T invoke(Class<?> clazz, Object obj, String methodName, Object[] args, boolean throwException) throws Exception {
      try {
         Method method = getMethod(clazz, methodName, args);
         method.setAccessible(true);
         return (T)method.invoke(obj, args);
      } catch (Exception var6) {
         if (throwException) {
            throw var6;
         } else {
            return null;
         }
      }
   }

   public static Method getMethod(Class<?> clazz, String methodName, Object[] args) throws NoSuchMethodException {
      Class[] argsClass = new Class[args.length];
      int i = 0;

      for(int j = args.length; i < j; ++i) {
         if (args[i] instanceof Boolean) {
            argsClass[i] = Boolean.TYPE;
         } else if (args[i] instanceof Integer) {
            argsClass[i] = Integer.TYPE;
         } else if (args[i] instanceof Long) {
            argsClass[i] = Long.TYPE;
         } else if (args[i] != null) {
            argsClass[i] = args[i].getClass();
         }
      }

      return getMethod(clazz, methodName, argsClass);
   }

   public static Method getMethod(Class<?> clazz, String methodDescriptor, String methodName) throws NoSuchMethodException {
      Method[] methods = clazz.getMethods();
      Method[] declaredMethods = methods;
      int var5 = methods.length;

      int var6;
      for(var6 = 0; var6 < var5; ++var6) {
         Method method = declaredMethods[var6];
         String desc = ASMUtils.desc(method);
         if (method.getName().equals(methodName) && desc.equals(methodDescriptor)) {
            return method;
         }
      }

      declaredMethods = clazz.getDeclaredMethods();
      Method[] var10 = declaredMethods;
      var6 = declaredMethods.length;

      for(int var11 = 0; var11 < var6; ++var11) {
         Method method = var10[var11];
         String desc = ASMUtils.desc(method);
         if (desc.equals(methodDescriptor)) {
            return method;
         }
      }

      throw new NoSuchMethodException(methodDescriptor + " descriptor");
   }

   public static Method getMethod(Class<?> clazz, String methodName, Class... parameterTypes) throws NoSuchMethodException {
      Method method;
      try {
         method = clazz.getMethod(methodName, parameterTypes);
      } catch (Exception var7) {
         try {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
         } catch (NoSuchMethodException var6) {
            return getMethodByName(clazz, methodName, parameterTypes);
         }
      }

      return method;
   }

   private static Method getMethodByName(Class<?> clazz, String methodName, Class... parameterTypes) throws NoSuchMethodException {
      if (clazz == Object.class) {
         throw new NoSuchMethodException();
      } else {
         Method[] methods = clazz.getDeclaredMethods();
         Method[] var4 = methods;
         int var5 = methods.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Method method = var4[var6];
            if (method.getName().equals(methodName)) {
               Class<?>[] methodParamTypes = method.getParameterTypes();
               if (methodParamTypes == null && parameterTypes == null) {
                  return method;
               }

               if (methodParamTypes != null && parameterTypes != null && methodParamTypes.length == parameterTypes.length) {
                  boolean match = true;

                  for(int i = 0; i < parameterTypes.length; ++i) {
                     if (parameterTypes[i] != null && !methodParamTypes[i].isAssignableFrom(parameterTypes[i])) {
                        match = false;
                        break;
                     }
                  }

                  if (match) {
                     return method;
                  }
               }
            }
         }

         return getMethodByName(clazz.getSuperclass(), methodName, parameterTypes);
      }
   }

   public static <T> T getFieldValue(Object object, String fieldName, boolean throwException) throws Exception {
      try {
         Field field = null;
         Class c = object.getClass();

         Exception exception;
         do {
            try {
               exception = null;
               field = c.getDeclaredField(fieldName);
            } catch (Exception var11) {
               exception = var11;
            } finally {
               c = c.getSuperclass();
            }
         } while(exception != null && c != null);

         if (exception != null) {
            throw exception;
         } else {
            field.setAccessible(true);
            return (T)field.get(object);
         }
      } catch (Exception var13) {
         if (throwException) {
            throw var13;
         } else {
            return null;
         }
      }
   }

   public static boolean isAssignableFrom(ClassLoader classLoader, Class<?> clazz, String clazzName) {
      if (clazz != null && clazzName != null) {
         if (!clazz.getName().equals(clazzName) && !clazz.getSimpleName().equals(clazzName)) {
            if (classLoader == null) {
               return false;
            } else {
               try {
                  Class<?> parentClazz = classLoader.loadClass(clazzName);
                  return parentClazz.isAssignableFrom(clazz);
               } catch (ClassNotFoundException var4) {
                  return false;
               }
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }
}
