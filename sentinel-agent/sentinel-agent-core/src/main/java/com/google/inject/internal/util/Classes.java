package com.google.inject.internal.util;

import com.google.common.base.Preconditions;

import java.lang.reflect.*;

public final class Classes {
   public static boolean isInnerClass(Class<?> clazz) {
      return !Modifier.isStatic(clazz.getModifiers()) && clazz.getEnclosingClass() != null;
   }

   public static boolean isConcrete(Class<?> clazz) {
      int modifiers = clazz.getModifiers();
      return !clazz.isInterface() && !Modifier.isAbstract(modifiers);
   }

   public static String toString(Member member) {
      Class<? extends Member> memberType = memberType(member);
      if (memberType == Method.class) {
         return member.getDeclaringClass().getName() + "." + member.getName() + "()";
      } else if (memberType == Field.class) {
         return member.getDeclaringClass().getName() + "." + member.getName();
      } else if (memberType == Constructor.class) {
         return member.getDeclaringClass().getName() + ".<init>()";
      } else {
         throw new AssertionError();
      }
   }

   public static Class<? extends Member> memberType(Member member) {
      Preconditions.checkNotNull(member, "member");
      if (member instanceof Field) {
         return Field.class;
      } else if (member instanceof Method) {
         return Method.class;
      } else if (member instanceof Constructor) {
         return Constructor.class;
      } else {
         throw new IllegalArgumentException("Unsupported implementation class for Member, " + member.getClass());
      }
   }
}
