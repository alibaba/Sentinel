/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.internal;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.ScopeAnnotation;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.Classes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation utilities.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class Annotations {

   /** Returns {@code true} if the given annotation type has no attributes. */
   public static boolean isMarker(Class<? extends Annotation> annotationType) {
      return annotationType.getDeclaredMethods().length == 0;
   }

   public static boolean isAllDefaultMethods(Class<? extends Annotation> annotationType) {
      boolean hasMethods = false;
      for (Method m : annotationType.getDeclaredMethods()) {
         hasMethods = true;
         if (m.getDefaultValue() == null) {
            return false;
         }
      }
      return hasMethods;
   }

   private static final LoadingCache<Class<? extends Annotation>, Annotation> cache =
           CacheBuilder.newBuilder()
                   .weakKeys()
                   .build(
                           new CacheLoader<Class<? extends Annotation>, Annotation>() {
                              @Override
                              public Annotation load(Class<? extends Annotation> input) {
                                 return generateAnnotationImpl(input);
                              }
                           });

   /**
    * Generates an Annotation for the annotation class. Requires that the annotation is all
    * optionals.
    */
   public static <T extends Annotation> T generateAnnotation(Class<T> annotationType) {
      Preconditions.checkState(
              isAllDefaultMethods(annotationType), "%s is not all default methods", annotationType);
      return (T) cache.getUnchecked(annotationType);
   }

   private static <T extends Annotation> T generateAnnotationImpl(final Class<T> annotationType) {
      final Map<String, Object> members = resolveMembers(annotationType);
      return annotationType.cast(
              Proxy.newProxyInstance(
                      annotationType.getClassLoader(),
                      new Class<?>[] {annotationType},
                      new InvocationHandler() {
                         @Override
                         public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                            String name = method.getName();
                            if (name.equals("annotationType")) {
                               return annotationType;
                            } else if (name.equals("toString")) {
                               return annotationToString(annotationType, members);
                            } else if (name.equals("hashCode")) {
                               return annotationHashCode(annotationType, members);
                            } else if (name.equals("equals")) {
                               return annotationEquals(annotationType, members, args[0]);
                            } else {
                               return members.get(name);
                            }
                         }
                      }));
   }

   private static ImmutableMap<String, Object> resolveMembers(
           Class<? extends Annotation> annotationType) {
      ImmutableMap.Builder<String, Object> result = ImmutableMap.builder();
      for (Method method : annotationType.getDeclaredMethods()) {
         result.put(method.getName(), method.getDefaultValue());
      }
      return result.build();
   }

   /** Implements {@link Annotation#equals}. */
   private static boolean annotationEquals(
           Class<? extends Annotation> type, Map<String, Object> members, Object other)
           throws Exception {
      if (!type.isInstance(other)) {
         return false;
      }
      for (Method method : type.getDeclaredMethods()) {
         String name = method.getName();
         if (!Arrays.deepEquals(
                 new Object[] {method.invoke(other)}, new Object[] {members.get(name)})) {
            return false;
         }
      }
      return true;
   }

   /** Implements {@link Annotation#hashCode}. */
   private static int annotationHashCode(
           Class<? extends Annotation> type, Map<String, Object> members) throws Exception {
      int result = 0;
      for (Method method : type.getDeclaredMethods()) {
         String name = method.getName();
         Object value = members.get(name);
         result += (127 * name.hashCode()) ^ (Arrays.deepHashCode(new Object[] {value}) - 31);
      }
      return result;
   }

   private static final MapJoiner JOINER = Joiner.on(", ").withKeyValueSeparator("=");

   private static final Function<Object, String> DEEP_TO_STRING_FN =
           new Function<Object, String>() {
              @Override
              public String apply(Object arg) {
                 String s = Arrays.deepToString(new Object[] {arg});
                 return s.substring(1, s.length() - 1); // cut off brackets
              }
           };

   /** Implements {@link Annotation#toString}. */
   private static String annotationToString(
           Class<? extends Annotation> type, Map<String, Object> members) throws Exception {
      StringBuilder sb = new StringBuilder().append("@").append(type.getName()).append("(");
      JOINER.appendTo(sb, Maps.transformValues(members, DEEP_TO_STRING_FN));
      return sb.append(")").toString();
   }

   /** Returns true if the given annotation is retained at runtime. */
   public static boolean isRetainedAtRuntime(Class<? extends Annotation> annotationType) {
      Retention retention = annotationType.getAnnotation(Retention.class);
      return retention != null && retention.value() == RetentionPolicy.RUNTIME;
   }

   /** Returns the scope annotation on {@code type}, or null if none is specified. */
   public static Class<? extends Annotation> findScopeAnnotation(
           Errors errors, Class<?> implementation) {
      return findScopeAnnotation(errors, implementation.getAnnotations());
   }

   /** Returns the scoping annotation, or null if there isn't one. */
   public static Class<? extends Annotation> findScopeAnnotation(
           Errors errors, Annotation[] annotations) {
      Class<? extends Annotation> found = null;

      for (Annotation annotation : annotations) {
         Class<? extends Annotation> annotationType = annotation.annotationType();
         if (isScopeAnnotation(annotationType)) {
            if (found != null) {
               errors.duplicateScopeAnnotations(found, annotationType);
            } else {
               found = annotationType;
            }
         }
      }

      return found;
   }

   static boolean containsComponentAnnotation(Annotation[] annotations) {
      for (Annotation annotation : annotations) {
         // TODO(user): Should we scope this down to dagger.Component?
         if (annotation.annotationType().getSimpleName().equals("Component")) {
            return true;
         }
      }

      return false;
   }

   private static final boolean QUOTE_MEMBER_VALUES = determineWhetherToQuote();

   /**
    * Returns {@code value}, quoted if annotation implementations quote their member values. In Java
    * 9, annotations quote their string members.
    */
   public static String memberValueString(String value) {
      return QUOTE_MEMBER_VALUES ? "\"" + value + "\"" : value;
   }

   @Retention(RUNTIME)
   private @interface TestAnnotation {
      String value();
   }

   @TestAnnotation("determineWhetherToQuote")
   private static boolean determineWhetherToQuote() {
      try {
         String annotation =
                 Annotations.class
                         .getDeclaredMethod("determineWhetherToQuote")
                         .getAnnotation(TestAnnotation.class)
                         .toString();
         return annotation.contains("\"determineWhetherToQuote\"");
      } catch (NoSuchMethodException e) {
         throw new AssertionError(e);
      }
   }

   /** Checks for the presence of annotations. Caches results because Android doesn't. */
   static class AnnotationChecker {
      private final Collection<Class<? extends Annotation>> annotationTypes;

      /** Returns true if the given class has one of the desired annotations. */
      private CacheLoader<Class<? extends Annotation>, Boolean> hasAnnotations =
              new CacheLoader<Class<? extends Annotation>, Boolean>() {
                 @Override
                 public Boolean load(Class<? extends Annotation> annotationType) {
                    for (Annotation annotation : annotationType.getAnnotations()) {
                       if (annotationTypes.contains(annotation.annotationType())) {
                          return true;
                       }
                    }
                    return false;
                 }
              };

      final LoadingCache<Class<? extends Annotation>, Boolean> cache =
              CacheBuilder.newBuilder().weakKeys().build(hasAnnotations);

      /** Constructs a new checker that looks for annotations of the given types. */
      AnnotationChecker(Collection<Class<? extends Annotation>> annotationTypes) {
         this.annotationTypes = annotationTypes;
      }

      /** Returns true if the given type has one of the desired annotations. */
      boolean hasAnnotations(Class<? extends Annotation> annotated) {
         return cache.getUnchecked(annotated);
      }
   }

   private static final AnnotationChecker scopeChecker =
           new AnnotationChecker(Arrays.asList(ScopeAnnotation.class, javax.inject.Scope.class));

   public static boolean isScopeAnnotation(Class<? extends Annotation> annotationType) {
      return scopeChecker.hasAnnotations(annotationType);
   }

   /**
    * Adds an error if there is a misplaced annotations on {@code type}. Scoping annotations are not
    * allowed on abstract classes or interfaces.
    */
   public static void checkForMisplacedScopeAnnotations(
           Class<?> type, Object source, Errors errors) {
      if (Classes.isConcrete(type)) {
         return;
      }

      Class<? extends Annotation> scopeAnnotation = findScopeAnnotation(errors, type);
      if (scopeAnnotation != null
              // We let Dagger Components through to aid migrations.
              && !containsComponentAnnotation(type.getAnnotations())) {
         errors.withSource(type).scopeAnnotationOnAbstractType(scopeAnnotation, type, source);
      }
   }

   // NOTE: getKey/findBindingAnnotation are used by Gin which is abandoned.  So changing this API
   // will prevent Gin users from upgrading Guice version.

   /** Gets a key for the given type, member and annotations. */
   public static Key<?> getKey(
           TypeLiteral<?> type, Member member, Annotation[] annotations, Errors errors)
           throws ErrorsException {
      int numErrorsBefore = errors.size();
      Annotation found = findBindingAnnotation(errors, member, annotations);
      errors.throwIfNewErrors(numErrorsBefore);
      return found == null ? Key.get(type) : Key.get(type, found);
   }

   /** Returns the binding annotation on {@code member}, or null if there isn't one. */
   public static Annotation findBindingAnnotation(
           Errors errors, Member member, Annotation[] annotations) {
      Annotation found = null;

      for (Annotation annotation : annotations) {
         Class<? extends Annotation> annotationType = annotation.annotationType();
         if (isBindingAnnotation(annotationType)) {
            if (found != null) {
               errors.duplicateBindingAnnotations(member, found.annotationType(), annotationType);
            } else {
               found = annotation;
            }
         }
      }

      return found;
   }

   private static final AnnotationChecker bindingAnnotationChecker =
           new AnnotationChecker(Arrays.asList(BindingAnnotation.class, Qualifier.class));

   /** Returns true if annotations of the specified type are binding annotations. */
   public static boolean isBindingAnnotation(Class<? extends Annotation> annotationType) {
      return bindingAnnotationChecker.hasAnnotations(annotationType);
   }

   /**
    * If the annotation is an instance of {@code javax.inject.Named}, canonicalizes to
    * com.google.guice.name.Named. Returns the given annotation otherwise.
    */
   public static Annotation canonicalizeIfNamed(Annotation annotation) {
      if (annotation instanceof javax.inject.Named) {
         return Names.named(((javax.inject.Named) annotation).value());
      } else {
         return annotation;
      }
   }

   /**
    * If the annotation is the class {@code javax.inject.Named}, canonicalizes to
    * com.google.guice.name.Named. Returns the given annotation class otherwise.
    */
   public static Class<? extends Annotation> canonicalizeIfNamed(
           Class<? extends Annotation> annotationType) {
      if (annotationType == javax.inject.Named.class) {
         return Named.class;
      } else {
         return annotationType;
      }
   }

   /**
    * Returns the name the binding should use. This is based on the annotation. If the annotation has
    * an instance and is not a marker annotation, we ask the annotation for its toString. If it was a
    * marker annotation or just an annotation type, we use the annotation's name. Otherwise, the name
    * is the empty string.
    */
   public static String nameOf(Key<?> key) {
      Annotation annotation = key.getAnnotation();
      Class<? extends Annotation> annotationType = key.getAnnotationType();
      if (annotation != null && !isMarker(annotationType)) {
         return key.getAnnotation().toString();
      } else if (key.getAnnotationType() != null) {
         return "@" + key.getAnnotationType().getName();
      } else {
         return "";
      }
   }
}
