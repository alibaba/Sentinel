package com.taobao.csp.ahas.module;

import com.alibaba.jvm.sandbox.api.filter.*;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.*;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.*;
import com.taobao.csp.ahas.service.bridge.aop.*;
import com.alibaba.jvm.sandbox.api.listener.*;

public class SandboxEnhancerFactory
{
   public static final String JAVA = "java.";
   public static final String SUN = "sun.";
   public static final String AHAS = "com.taobao.csp.ahas";

   public static Filter createFilter(final String enhancerClassName, final PointCut pointCut,ClassLoader classLoader) {
      return new Filter() {
         public boolean doClassFilter(final int access, final String javaClassName, final String superClassTypeJavaClassName, final String[] interfaceTypeJavaClassNameArray, final String[] annotationTypeJavaClassNameArray) {
            if (javaClassName.startsWith("java.") || javaClassName.startsWith("sun.") || javaClassName.startsWith("[") || javaClassName.startsWith("com.taobao.csp.ahas")) {
               return false;
            }
          //  final ClassLoader classLoader = ;
            final ClassMatcher classMatcher = pointCut.getClassMatcher();
            return classMatcher.isMatched(javaClassName, new ClassInfo(access, javaClassName, superClassTypeJavaClassName, interfaceTypeJavaClassNameArray, annotationTypeJavaClassNameArray, classLoader));
         }

         public boolean doMethodFilter(final int access, final String javaMethodName, final String[] parameterTypeJavaClassNameArray, final String[] throwsTypeJavaClassNameArray, final String[] annotationTypeJavaClassNameArray) {
            final MethodMatcher methodMatcher = pointCut.getMethodMatcher();
            final boolean match = methodMatcher.isMatched(javaMethodName, new MethodInfo(access, javaMethodName, parameterTypeJavaClassNameArray, throwsTypeJavaClassNameArray, annotationTypeJavaClassNameArray, (String)null));
            if (match) {
               AopService.addEnhancerClass(enhancerClassName);
            }
            return match;
         }
      };
   }

   public static EventListener createBeforeEventListener(final AspectPlugin plugin) {
      return (EventListener)new BeforeEventListener(plugin);
   }

   public static EventListener createAfterEventListener(final AspectPlugin plugin) {
      return (EventListener)new AfterEventListener(plugin);
   }

   public static EventListener createThrowsEventListener(final AspectPlugin plugin) {
      return (EventListener)new ThrowsEventListener(plugin);
   }
}
