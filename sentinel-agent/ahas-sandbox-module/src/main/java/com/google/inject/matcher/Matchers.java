package com.google.inject.matcher;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class Matchers {
   private static final Matcher<Object> ANY = new Any();

   private Matchers() {
   }

   public static Matcher<Object> any() {
      return ANY;
   }

   public static <T> Matcher<T> not(Matcher<? super T> p) {
      return new Not(p);
   }

   private static void checkForRuntimeRetention(Class<? extends Annotation> annotationType) {
      Retention retention = (Retention)annotationType.getAnnotation(Retention.class);
      Preconditions.checkArgument(retention != null && retention.value() == RetentionPolicy.RUNTIME, "Annotation %s is missing RUNTIME retention", annotationType.getSimpleName());
   }

   public static Matcher<AnnotatedElement> annotatedWith(Class<? extends Annotation> annotationType) {
      return new AnnotatedWithType(annotationType);
   }

   public static com.google.inject.matcher.Matcher<AnnotatedElement> annotatedWith(Annotation annotation) {
      return new AnnotatedWith(annotation);
   }

   public static Matcher<Class> subclassesOf(Class<?> superclass) {
      return new SubclassesOf(superclass);
   }

   public static Matcher<Object> only(Object value) {
      return new Only(value);
   }

   public static Matcher<Object> identicalTo(Object value) {
      return new IdenticalTo(value);
   }

   public static Matcher<Class> inPackage(Package targetPackage) {
      return new InPackage(targetPackage);
   }

   public static Matcher<Class> inSubpackage(String targetPackageName) {
      return new InSubpackage(targetPackageName);
   }

   public static com.google.inject.matcher.Matcher<Method> returns(Matcher<? super Class<?>> returnType) {
      return new Returns(returnType);
   }

   private static class Returns extends AbstractMatcher<Method> implements Serializable {
      private final Matcher<? super Class<?>> returnType;
      private static final long serialVersionUID = 0L;

      public Returns(Matcher<? super Class<?>> returnType) {
         this.returnType = (Matcher)Preconditions.checkNotNull(returnType, "return type matcher");
      }

      public boolean matches(Method m) {
         return this.returnType.matches(m.getReturnType());
      }

      public boolean equals(Object other) {
         return other instanceof Returns && ((Returns)other).returnType.equals(this.returnType);
      }

      public int hashCode() {
         return 37 * this.returnType.hashCode();
      }

      public String toString() {
         return "returns(" + this.returnType + ")";
      }
   }

   private static class InSubpackage extends AbstractMatcher<Class> implements Serializable {
      private final String targetPackageName;
      private static final long serialVersionUID = 0L;

      public InSubpackage(String targetPackageName) {
         this.targetPackageName = targetPackageName;
      }

      public boolean matches(Class c) {
         String classPackageName = c.getPackage().getName();
         return classPackageName.equals(this.targetPackageName) || classPackageName.startsWith(this.targetPackageName + ".");
      }

      public boolean equals(Object other) {
         return other instanceof InSubpackage && ((InSubpackage)other).targetPackageName.equals(this.targetPackageName);
      }

      public int hashCode() {
         return 37 * this.targetPackageName.hashCode();
      }

      public String toString() {
         return "inSubpackage(" + this.targetPackageName + ")";
      }
   }

   private static class InPackage extends AbstractMatcher<Class> implements Serializable {
      private final transient Package targetPackage;
      private final String packageName;
      private static final long serialVersionUID = 0L;

      public InPackage(Package targetPackage) {
         this.targetPackage = (Package)Preconditions.checkNotNull(targetPackage, "package");
         this.packageName = targetPackage.getName();
      }

      public boolean matches(Class c) {
         return c.getPackage().equals(this.targetPackage);
      }

      public boolean equals(Object other) {
         return other instanceof InPackage && ((InPackage)other).targetPackage.equals(this.targetPackage);
      }

      public int hashCode() {
         return 37 * this.targetPackage.hashCode();
      }

      public String toString() {
         return "inPackage(" + this.targetPackage.getName() + ")";
      }

      public Object readResolve() {
         return Matchers.inPackage(Package.getPackage(this.packageName));
      }
   }

   private static class IdenticalTo extends AbstractMatcher<Object> implements Serializable {
      private final Object value;
      private static final long serialVersionUID = 0L;

      public IdenticalTo(Object value) {
         this.value = Preconditions.checkNotNull(value, "value");
      }

      public boolean matches(Object other) {
         return this.value == other;
      }

      public boolean equals(Object other) {
         return other instanceof IdenticalTo && ((IdenticalTo)other).value == this.value;
      }

      public int hashCode() {
         return 37 * System.identityHashCode(this.value);
      }

      public String toString() {
         return "identicalTo(" + this.value + ")";
      }
   }

   private static class Only extends AbstractMatcher<Object> implements Serializable {
      private final Object value;
      private static final long serialVersionUID = 0L;

      public Only(Object value) {
         this.value = Preconditions.checkNotNull(value, "value");
      }

      public boolean matches(Object other) {
         return this.value.equals(other);
      }

      public boolean equals(Object other) {
         return other instanceof Only && ((Only)other).value.equals(this.value);
      }

      public int hashCode() {
         return 37 * this.value.hashCode();
      }

      public String toString() {
         return "only(" + this.value + ")";
      }
   }

   private static class SubclassesOf extends AbstractMatcher<Class> implements Serializable {
      private final Class<?> superclass;
      private static final long serialVersionUID = 0L;

      public SubclassesOf(Class<?> superclass) {
         this.superclass = (Class)Preconditions.checkNotNull(superclass, "superclass");
      }

      public boolean matches(Class subclass) {
         return this.superclass.isAssignableFrom(subclass);
      }

      public boolean equals(Object other) {
         return other instanceof SubclassesOf && ((SubclassesOf)other).superclass.equals(this.superclass);
      }

      public int hashCode() {
         return 37 * this.superclass.hashCode();
      }

      public String toString() {
         return "subclassesOf(" + this.superclass.getSimpleName() + ".class)";
      }
   }

   private static class AnnotatedWith extends AbstractMatcher<AnnotatedElement> implements Serializable {
      private final Annotation annotation;
      private static final long serialVersionUID = 0L;

      public AnnotatedWith(Annotation annotation) {
         this.annotation = (Annotation)Preconditions.checkNotNull(annotation, "annotation");
         Matchers.checkForRuntimeRetention(annotation.annotationType());
      }

      public boolean matches(AnnotatedElement element) {
         Annotation fromElement = element.getAnnotation(this.annotation.annotationType());
         return fromElement != null && this.annotation.equals(fromElement);
      }

      public boolean equals(Object other) {
         return other instanceof AnnotatedWith && ((AnnotatedWith)other).annotation.equals(this.annotation);
      }

      public int hashCode() {
         return 37 * this.annotation.hashCode();
      }

      public String toString() {
         return "annotatedWith(" + this.annotation + ")";
      }
   }

   private static class AnnotatedWithType extends AbstractMatcher<AnnotatedElement> implements Serializable {
      private final Class<? extends Annotation> annotationType;
      private static final long serialVersionUID = 0L;

      public AnnotatedWithType(Class<? extends Annotation> annotationType) {
         this.annotationType = (Class)Preconditions.checkNotNull(annotationType, "annotation type");
         Matchers.checkForRuntimeRetention(annotationType);
      }

      public boolean matches(AnnotatedElement element) {
         return element.isAnnotationPresent(this.annotationType);
      }

      public boolean equals(Object other) {
         return other instanceof AnnotatedWithType && ((AnnotatedWithType)other).annotationType.equals(this.annotationType);
      }

      public int hashCode() {
         return 37 * this.annotationType.hashCode();
      }

      public String toString() {
         return "annotatedWith(" + this.annotationType.getSimpleName() + ".class)";
      }
   }

   private static class Not<T> extends AbstractMatcher<T> implements Serializable {
      final Matcher<? super T> delegate;
      private static final long serialVersionUID = 0L;

      private Not(Matcher<? super T> delegate) {
         this.delegate = (Matcher)Preconditions.checkNotNull(delegate, "delegate");
      }

      public boolean matches(T t) {
         return !this.delegate.matches(t);
      }

      public boolean equals(Object other) {
         return other instanceof Not && ((Not)other).delegate.equals(this.delegate);
      }

      public int hashCode() {
         return -this.delegate.hashCode();
      }

      public String toString() {
         return "not(" + this.delegate + ")";
      }

      // $FF: synthetic method
      Not(Matcher x0, Object x1) {
         this(x0);
      }
   }

   private static class Any extends AbstractMatcher<Object> implements Serializable {
      private static final long serialVersionUID = 0L;

      private Any() {
      }

      public boolean matches(Object o) {
         return true;
      }

      public String toString() {
         return "any()";
      }

      public Object readResolve() {
         return Matchers.any();
      }

      // $FF: synthetic method
      Any(Object x0) {
         this();
      }
   }
}
