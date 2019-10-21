/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.inject.spi;

import static com.google.inject.internal.MoreTypes.getRawType;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.inject.*;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.internal.Nullability;
import com.google.inject.internal.util.Classes;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A constructor, field or method that can receive injections. Typically this is a member with the
 * {@literal @}{@link Inject} annotation. For non-private, no argument constructors, the member may
 * omit the annotation.
 *
 * @author crazybob@google.com (Bob Lee)
 * @since 2.0
 */
public final class InjectionPoint {

   private static final Logger logger = Logger.getLogger(InjectionPoint.class.getName());

   private final boolean optional;
   private final Member member;
   private final TypeLiteral<?> declaringType;
   private final ImmutableList<com.google.inject.spi.Dependency<?>> dependencies;

   InjectionPoint(TypeLiteral<?> declaringType, Method method, boolean optional) {
      this.member = method;
      this.declaringType = declaringType;
      this.optional = optional;
      this.dependencies = forMember(method, declaringType, method.getParameterAnnotations());
   }

   InjectionPoint(TypeLiteral<?> declaringType, Constructor<?> constructor) {
      this.member = constructor;
      this.declaringType = declaringType;
      this.optional = false;
      this.dependencies =
              forMember(constructor, declaringType, constructor.getParameterAnnotations());
   }

   InjectionPoint(TypeLiteral<?> declaringType, Field field, boolean optional) {
      this.member = field;
      this.declaringType = declaringType;
      this.optional = optional;

      Annotation[] annotations = field.getAnnotations();

      Errors errors = new Errors(field);
      Key<?> key = null;
      try {
         key = Annotations.getKey(declaringType.getFieldType(field), field, annotations, errors);
      } catch (ConfigurationException e) {
         errors.merge(e.getErrorMessages());
      } catch (ErrorsException e) {
         errors.merge(e.getErrors());
      }
      errors.throwConfigurationExceptionIfErrorsExist();

      this.dependencies =
              ImmutableList.<Dependency<?>>of(
                      newDependency(key, Nullability.allowsNull(annotations), -1));
   }

   private ImmutableList<Dependency<?>> forMember(
           Member member, TypeLiteral<?> type, Annotation[][] paramterAnnotations) {
      Errors errors = new Errors(member);

      List<Dependency<?>> dependencies = Lists.newArrayList();
      int index = 0;

      for (TypeLiteral<?> parameterType : type.getParameterTypes(member)) {
         try {
            Annotation[] parameterAnnotations = paramterAnnotations[index];
            Key<?> key = Annotations.getKey(parameterType, member, parameterAnnotations, errors);
            dependencies.add(newDependency(key, Nullability.allowsNull(parameterAnnotations), index));
            index++;
         } catch (ConfigurationException e) {
            errors.merge(e.getErrorMessages());
         } catch (ErrorsException e) {
            errors.merge(e.getErrors());
         }
      }

      errors.throwConfigurationExceptionIfErrorsExist();
      return ImmutableList.copyOf(dependencies);
   }

   // This metohd is necessary to create a Dependency<T> with proper generic type information
   private <T> Dependency<T> newDependency(Key<T> key, boolean allowsNull, int parameterIndex) {
      return new Dependency<T>(this, key, allowsNull, parameterIndex);
   }

   /** Returns the injected constructor, field, or method. */
   public Member getMember() {
      // TODO: Don't expose the original member (which probably has setAccessible(true)).
      return member;
   }

   /**
    * Returns the dependencies for this injection point. If the injection point is for a method or
    * constructor, the dependencies will correspond to that member's parameters. Field injection
    * points always have a single dependency for the field itself.
    *
    * @return a possibly-empty list
    */
   public List<Dependency<?>> getDependencies() {
      return dependencies;
   }

   /**
    * Returns true if this injection point shall be skipped if the injector cannot resolve bindings
    * for all required dependencies. Both explicit bindings (as specified in a module), and implicit
    * bindings ({@literal @}{@link ImplementedBy ImplementedBy}, default
    * constructors etc.) may be used to satisfy optional injection points.
    */
   public boolean isOptional() {
      return optional;
   }

   /**
    * Returns true if the element is annotated with {@literal @}{@link Toolable}.
    *
    * @since 3.0
    */
   public boolean isToolable() {
      return ((AnnotatedElement) member).isAnnotationPresent(Toolable.class);
   }

   /**
    * Returns the generic type that defines this injection point. If the member exists on a
    * parameterized type, the result will include more type information than the member's {@link
    * Member#getDeclaringClass() raw declaring class}.
    *
    * @since 3.0
    */
   public TypeLiteral<?> getDeclaringType() {
      return declaringType;
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof InjectionPoint
              && member.equals(((InjectionPoint) o).member)
              && declaringType.equals(((InjectionPoint) o).declaringType);
   }

   @Override
   public int hashCode() {
      return member.hashCode() ^ declaringType.hashCode();
   }

   @Override
   public String toString() {
      return Classes.toString(member);
   }

   /**
    * Returns a new injection point for the specified constructor. If the declaring type of {@code
    * constructor} is parameterized (such as {@code List<T>}), prefer the overload that includes a
    * type literal.
    *
    * @param constructor any single constructor present on {@code type}.
    * @since 3.0
    */
   public static <T> InjectionPoint forConstructor(Constructor<T> constructor) {
      return new InjectionPoint(TypeLiteral.get(constructor.getDeclaringClass()), constructor);
   }

   /**
    * Returns a new injection point for the specified constructor of {@code type}.
    *
    * @param constructor any single constructor present on {@code type}.
    * @param type the concrete type that defines {@code constructor}.
    * @since 3.0
    */
   public static <T> InjectionPoint forConstructor(
           Constructor<T> constructor, TypeLiteral<? extends T> type) {
      if (type.getRawType() != constructor.getDeclaringClass()) {
         new Errors(type)
                 .constructorNotDefinedByType(constructor, type)
                 .throwConfigurationExceptionIfErrorsExist();
      }

      return new InjectionPoint(type, constructor);
   }

   /**
    * Returns a new injection point for the injectable constructor of {@code type}.
    *
    * @param type a concrete type with exactly one constructor annotated {@literal @}{@link Inject},
    *     or a no-arguments constructor that is not private.
    * @throws ConfigurationException if there is no injectable constructor, more than one injectable
    *     constructor, or if parameters of the injectable constructor are malformed, such as a
    *     parameter with multiple binding annotations.
    */
   public static InjectionPoint forConstructorOf(TypeLiteral<?> type) {
      Class<?> rawType = getRawType(type.getType());
      Errors errors = new Errors(rawType);

      Constructor<?> injectableConstructor = null;
      for (Constructor<?> constructor : rawType.getDeclaredConstructors()) {

         boolean optional;
         Inject guiceInject = constructor.getAnnotation(Inject.class);
         if (guiceInject == null) {
            javax.inject.Inject javaxInject = constructor.getAnnotation(javax.inject.Inject.class);
            if (javaxInject == null) {
               continue;
            }
            optional = false;
         } else {
            optional = guiceInject.optional();
         }

         if (optional) {
            errors.optionalConstructor(constructor);
         }

         if (injectableConstructor != null) {
            errors.tooManyConstructors(rawType);
         }

         injectableConstructor = constructor;
         checkForMisplacedBindingAnnotations(injectableConstructor, errors);
      }

      errors.throwConfigurationExceptionIfErrorsExist();

      if (injectableConstructor != null) {
         return new InjectionPoint(type, injectableConstructor);
      }

      // If no annotated constructor is found, look for a no-arg constructor instead.
      try {
         Constructor<?> noArgConstructor = rawType.getDeclaredConstructor();

         // Disallow private constructors on non-private classes (unless they have @Inject)
         if (Modifier.isPrivate(noArgConstructor.getModifiers())
                 && !Modifier.isPrivate(rawType.getModifiers())) {
            errors.missingConstructor(rawType);
            throw new ConfigurationException(errors.getMessages());
         }

         checkForMisplacedBindingAnnotations(noArgConstructor, errors);
         return new InjectionPoint(type, noArgConstructor);
      } catch (NoSuchMethodException e) {
         errors.missingConstructor(rawType);
         throw new ConfigurationException(errors.getMessages());
      }
   }

   /**
    * Returns a new injection point for the injectable constructor of {@code type}.
    *
    * @param type a concrete type with exactly one constructor annotated {@literal @}{@link Inject},
    *     or a no-arguments constructor that is not private.
    * @throws ConfigurationException if there is no injectable constructor, more than one injectable
    *     constructor, or if parameters of the injectable constructor are malformed, such as a
    *     parameter with multiple binding annotations.
    */
   public static InjectionPoint forConstructorOf(Class<?> type) {
      return forConstructorOf(TypeLiteral.get(type));
   }

   /**
    * Returns a new injection point for the specified method of {@code type}. This is useful for
    * extensions that need to build dependency graphs from arbitrary methods.
    *
    * @param method any single method present on {@code type}.
    * @param type the concrete type that defines {@code method}.
    * @since 4.0
    */
   public static <T> InjectionPoint forMethod(Method method, TypeLiteral<T> type) {
      return new InjectionPoint(type, method, false);
   }

   /**
    * Returns all static method and field injection points on {@code type}.
    *
    * @return a possibly empty set of injection points. The set has a specified iteration order. All
    *     fields are returned and then all methods. Within the fields, supertype fields are returned
    *     before subtype fields. Similarly, supertype methods are returned before subtype methods.
    * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as
    *     a field with multiple binding annotations. The exception's {@link
    *     ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>} of
    *     the valid injection points.
    */
   public static Set<InjectionPoint> forStaticMethodsAndFields(TypeLiteral<?> type) {
      Errors errors = new Errors();

      Set<InjectionPoint> result;

      if (type.getRawType().isInterface()) {
         errors.staticInjectionOnInterface(type.getRawType());
         result = null;
      } else {
         result = getInjectionPoints(type, true, errors);
      }

      if (errors.hasErrors()) {
         throw new ConfigurationException(errors.getMessages()).withPartialValue(result);
      }
      return result;
   }

   /**
    * Returns all static method and field injection points on {@code type}.
    *
    * @return a possibly empty set of injection points. The set has a specified iteration order. All
    *     fields are returned and then all methods. Within the fields, supertype fields are returned
    *     before subtype fields. Similarly, supertype methods are returned before subtype methods.
    * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as
    *     a field with multiple binding annotations. The exception's {@link
    *     ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>} of
    *     the valid injection points.
    */
   public static Set<InjectionPoint> forStaticMethodsAndFields(Class<?> type) {
      return forStaticMethodsAndFields(TypeLiteral.get(type));
   }

   /**
    * Returns all instance method and field injection points on {@code type}.
    *
    * @return a possibly empty set of injection points. The set has a specified iteration order. All
    *     fields are returned and then all methods. Within the fields, supertype fields are returned
    *     before subtype fields. Similarly, supertype methods are returned before subtype methods.
    * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as
    *     a field with multiple binding annotations. The exception's {@link
    *     ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>} of
    *     the valid injection points.
    */
   public static Set<InjectionPoint> forInstanceMethodsAndFields(TypeLiteral<?> type) {
      Errors errors = new Errors();
      Set<InjectionPoint> result = getInjectionPoints(type, false, errors);
      if (errors.hasErrors()) {
         throw new ConfigurationException(errors.getMessages()).withPartialValue(result);
      }
      return result;
   }

   /**
    * Returns all instance method and field injection points on {@code type}.
    *
    * @return a possibly empty set of injection points. The set has a specified iteration order. All
    *     fields are returned and then all methods. Within the fields, supertype fields are returned
    *     before subtype fields. Similarly, supertype methods are returned before subtype methods.
    * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as
    *     a field with multiple binding annotations. The exception's {@link
    *     ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>} of
    *     the valid injection points.
    */
   public static Set<InjectionPoint> forInstanceMethodsAndFields(Class<?> type) {
      return forInstanceMethodsAndFields(TypeLiteral.get(type));
   }

   /** Returns true if the binding annotation is in the wrong place. */
   private static boolean checkForMisplacedBindingAnnotations(Member member, Errors errors) {
      Annotation misplacedBindingAnnotation =
              Annotations.findBindingAnnotation(
                      errors, member, ((AnnotatedElement) member).getAnnotations());
      if (misplacedBindingAnnotation == null) {
         return false;
      }

      // don't warn about misplaced binding annotations on methods when there's a field with the same
      // name. In Scala, fields always get accessor methods (that we need to ignore). See bug 242.
      if (member instanceof Method) {
         try {
            if (member.getDeclaringClass().getDeclaredField(member.getName()) != null) {
               return false;
            }
         } catch (NoSuchFieldException ignore) {
         }
      }

      errors.misplacedBindingAnnotation(member, misplacedBindingAnnotation);
      return true;
   }

   /** Node in the doubly-linked list of injectable members (fields and methods). */
   abstract static class InjectableMember {
      final TypeLiteral<?> declaringType;
      final boolean optional;
      final boolean jsr330;
      InjectableMember previous;
      InjectableMember next;

      InjectableMember(TypeLiteral<?> declaringType, Annotation atInject) {
         this.declaringType = declaringType;

         if (atInject.annotationType() == javax.inject.Inject.class) {
            optional = false;
            jsr330 = true;
            return;
         }

         jsr330 = false;
         optional = ((Inject) atInject).optional();
      }

      abstract InjectionPoint toInjectionPoint();
   }

   static class InjectableField extends InjectableMember {
      final Field field;

      InjectableField(TypeLiteral<?> declaringType, Field field, Annotation atInject) {
         super(declaringType, atInject);
         this.field = field;
      }

      @Override
      InjectionPoint toInjectionPoint() {
         return new InjectionPoint(declaringType, field, optional);
      }
   }

   static class InjectableMethod extends InjectableMember {
      final Method method;
      /**
       * true if this method overrode a method that was annotated with com.google.inject.Inject. used
       * to allow different override behavior for guice inject vs javax.inject.Inject
       */
      boolean overrodeGuiceInject;

      InjectableMethod(TypeLiteral<?> declaringType, Method method, Annotation atInject) {
         super(declaringType, atInject);
         this.method = method;
      }

      @Override
      InjectionPoint toInjectionPoint() {
         return new InjectionPoint(declaringType, method, optional);
      }

      public boolean isFinal() {
         return Modifier.isFinal(method.getModifiers());
      }
   }

   static Annotation getAtInject(AnnotatedElement member) {
      Annotation a = member.getAnnotation(javax.inject.Inject.class);
      return a == null ? member.getAnnotation(Inject.class) : a;
   }

   /** Linked list of injectable members. */
   static class InjectableMembers {
      InjectableMember head;
      InjectableMember tail;

      void add(InjectableMember member) {
         if (head == null) {
            head = tail = member;
         } else {
            member.previous = tail;
            tail.next = member;
            tail = member;
         }
      }

      void remove(InjectableMember member) {
         if (member.previous != null) {
            member.previous.next = member.next;
         }
         if (member.next != null) {
            member.next.previous = member.previous;
         }
         if (head == member) {
            head = member.next;
         }
         if (tail == member) {
            tail = member.previous;
         }
      }

      boolean isEmpty() {
         return head == null;
      }
   }

   /** Position in type hierarchy. */
   enum Position {
      TOP, // No need to check for overridden methods
      MIDDLE,
      BOTTOM // Methods won't be overridden
   }

   /**
    * Keeps track of injectable methods so we can remove methods that get overridden in O(1) time.
    * Uses our position in the type hierarchy to perform optimizations.
    */
   static class OverrideIndex {
      final InjectableMembers injectableMembers;
      Map<Signature, List<InjectableMethod>> bySignature;
      Position position = Position.TOP;

      OverrideIndex(InjectableMembers injectableMembers) {
         this.injectableMembers = injectableMembers;
      }

      /* Caches the signature for the last method. */
      Method lastMethod;
      Signature lastSignature;

      /**
       * Removes a method overridden by the given method, if present. In order to remain backwards
       * compatible with prior Guice versions, this will *not* remove overridden methods if
       * 'alwaysRemove' is false and the overridden signature was annotated with a
       * com.google.inject.Inject.
       *
       * @param method The method used to determine what is overridden and should be removed.
       * @param alwaysRemove true if overridden methods should be removed even if they were
       *     guice @Inject
       * @param injectableMethod if this method overrode any guice @Inject methods, {@link
       *     InjectableMethod#overrodeGuiceInject} is set to true
       */
      boolean removeIfOverriddenBy(
              Method method, boolean alwaysRemove, InjectableMethod injectableMethod) {
         if (position == Position.TOP) {
            // If we're at the top of the hierarchy, there's nothing to override.
            return false;
         }

         if (bySignature == null) {
            // We encountered a method in a subclass. Time to index the
            // methods in the parent class.
            bySignature = new HashMap<>();
            for (InjectableMember member = injectableMembers.head;
                 member != null;
                 member = member.next) {
               if (!(member instanceof InjectableMethod)) {
                  continue;
               }
               InjectableMethod im = (InjectableMethod) member;
               if (im.isFinal()) {
                  continue;
               }
               List<InjectableMethod> methods = new ArrayList<>();
               methods.add(im);
               bySignature.put(new Signature(im.method), methods);
            }
         }

         lastMethod = method;
         Signature signature = lastSignature = new Signature(method);
         List<InjectableMethod> methods = bySignature.get(signature);
         boolean removed = false;
         if (methods != null) {
            for (Iterator<InjectableMethod> iterator = methods.iterator(); iterator.hasNext(); ) {
               InjectableMethod possiblyOverridden = iterator.next();
               if (overrides(method, possiblyOverridden.method)) {
                  boolean wasGuiceInject =
                          !possiblyOverridden.jsr330 || possiblyOverridden.overrodeGuiceInject;
                  if (injectableMethod != null) {
                     injectableMethod.overrodeGuiceInject = wasGuiceInject;
                  }
                  // Only actually remove the methods if we want to force
                  // remove or if the signature never specified @com.google.inject.Inject
                  // somewhere.
                  if (alwaysRemove || !wasGuiceInject) {
                     removed = true;
                     iterator.remove();
                     injectableMembers.remove(possiblyOverridden);
                  }
               }
            }
         }
         return removed;
      }

      /**
       * Adds the given method to the list of injection points. Keeps track of it in this index in
       * case it gets overridden.
       */
      void add(InjectableMethod injectableMethod) {
         injectableMembers.add(injectableMethod);
         if (position == Position.BOTTOM || injectableMethod.isFinal()) {
            // This method can't be overridden, so there's no need to index it.
            return;
         }
         if (bySignature != null) {
            // Try to reuse the signature we created during removal
            @SuppressWarnings("ReferenceEquality")
            Signature signature =
                    injectableMethod.method == lastMethod
                            ? lastSignature
                            : new Signature(injectableMethod.method);
            List<InjectableMethod> methods = bySignature.get(signature);
            if (methods == null) {
               methods = new ArrayList<>();
               bySignature.put(signature, methods);
            }
            methods.add(injectableMethod);
         }
      }
   }

   /**
    * Returns an ordered, immutable set of injection points for the given type. Members in
    * superclasses come before members in subclasses. Within a class, fields come before methods.
    * Overridden methods are filtered out. The order of fields/methods within a class is consistent
    * but undefined.
    *
    * @param statics true is this method should return static members, false for instance members
    * @param errors used to record errors
    */
   private static Set<InjectionPoint> getInjectionPoints(
           final TypeLiteral<?> type, boolean statics, Errors errors) {
      InjectableMembers injectableMembers = new InjectableMembers();
      OverrideIndex overrideIndex = null;

      List<TypeLiteral<?>> hierarchy = hierarchyFor(type);
      int topIndex = hierarchy.size() - 1;
      for (int i = topIndex; i >= 0; i--) {
         if (overrideIndex != null && i < topIndex) {
            // Knowing the position within the hierarchy helps us make optimizations.
            if (i == 0) {
               overrideIndex.position = Position.BOTTOM;
            } else {
               overrideIndex.position = Position.MIDDLE;
            }
         }

         TypeLiteral<?> current = hierarchy.get(i);

         for (Field field : getDeclaredFields(current)) {
            if (Modifier.isStatic(field.getModifiers()) == statics) {
               Annotation atInject = getAtInject(field);
               if (atInject != null) {
                  InjectableField injectableField = new InjectableField(current, field, atInject);
                  if (injectableField.jsr330 && Modifier.isFinal(field.getModifiers())) {
                     errors.cannotInjectFinalField(field);
                  }
                  injectableMembers.add(injectableField);
               }
            }
         }

         for (Method method : getDeclaredMethods(current)) {
            if (isEligibleForInjection(method, statics)) {
               Annotation atInject = getAtInject(method);
               if (atInject != null) {
                  InjectableMethod injectableMethod = new InjectableMethod(current, method, atInject);
                  if (checkForMisplacedBindingAnnotations(method, errors)
                          || !isValidMethod(injectableMethod, errors)) {
                     if (overrideIndex != null) {
                        boolean removed =
                                overrideIndex.removeIfOverriddenBy(method, false, injectableMethod);
                        if (removed) {
                           logger.log(
                                   Level.WARNING,
                                   "Method: {0} is not a valid injectable method ("
                                           + "because it either has misplaced binding annotations "
                                           + "or specifies type parameters) but is overriding a method that is "
                                           + "valid. Because it is not valid, the method will not be injected. "
                                           + "To fix this, make the method a valid injectable method.",
                                   method);
                        }
                     }
                     continue;
                  }
                  if (statics) {
                     injectableMembers.add(injectableMethod);
                  } else {
                     if (overrideIndex == null) {
                        /*
                         * Creating the override index lazily means that the first type in the hierarchy
                         * with injectable methods (not necessarily the top most type) will be treated as
                         * the TOP position and will enjoy the same optimizations (no checks for overridden
                         * methods, etc.).
                         */
                        overrideIndex = new OverrideIndex(injectableMembers);
                     } else {
                        // Forcibly remove the overridden method, otherwise we'll inject
                        // it twice.
                        overrideIndex.removeIfOverriddenBy(method, true, injectableMethod);
                     }
                     overrideIndex.add(injectableMethod);
                  }
               } else {
                  if (overrideIndex != null) {
                     boolean removed = overrideIndex.removeIfOverriddenBy(method, false, null);
                     if (removed) {
                        logger.log(
                                Level.WARNING,
                                "Method: {0} is not annotated with @Inject but "
                                        + "is overriding a method that is annotated with @javax.inject.Inject."
                                        + "Because it is not annotated with @Inject, the method will not be "
                                        + "injected. To fix this, annotate the method with @Inject.",
                                method);
                     }
                  }
               }
            }
         }
      }

      if (injectableMembers.isEmpty()) {
         return Collections.emptySet();
      }

      ImmutableSet.Builder<InjectionPoint> builder = ImmutableSet.builder();
      for (InjectableMember im = injectableMembers.head; im != null; im = im.next) {
         try {
            builder.add(im.toInjectionPoint());
         } catch (ConfigurationException ignorable) {
            if (!im.optional) {
               errors.merge(ignorable.getErrorMessages());
            }
         }
      }
      return builder.build();
   }

   private static Field[] getDeclaredFields(TypeLiteral<?> type) {
      Field[] fields = type.getRawType().getDeclaredFields();
      Arrays.sort(fields, FIELD_ORDERING);
      return fields;
   }

   private static Method[] getDeclaredMethods(TypeLiteral<?> type) {
      Method[] methods = type.getRawType().getDeclaredMethods();
      Arrays.sort(methods, METHOD_ORDERING);
      return methods;
   }

   /**
    * An ordering suitable for comparing two classes if they are loaded by the same classloader
    *
    * <p>Within a single classloader there can only be one class with a given name, so we just
    * compare the names.
    */
   private static final Ordering<Class<?>> CLASS_ORDERING =
           new Ordering<Class<?>>() {
              @Override
              public int compare(Class<?> o1, Class<?> o2) {
                 return o1.getName().compareTo(o2.getName());
              }
           };

   /**
    * An ordering suitable for comparing two fields if they are owned by the same class.
    *
    * <p>Within a single class it is sufficent to compare the non-generic field signature which
    * consists of the field name and type.
    */
   private static final Ordering<Field> FIELD_ORDERING =
           new Ordering<Field>() {
              @Override
              public int compare(Field left, Field right) {
                 return ComparisonChain.start()
                         .compare(left.getName(), right.getName())
                         .compare(left.getType(), right.getType(), CLASS_ORDERING)
                         .result();
              }
           };

   /**
    * An ordering suitable for comparing two methods if they are owned by the same class.
    *
    * <p>Within a single class it is sufficient to compare the non-generic method signature which
    * consists of the name, return type and parameter types.
    */
   private static final Ordering<Method> METHOD_ORDERING =
           new Ordering<Method>() {
              @Override
              public int compare(Method left, Method right) {
                 return ComparisonChain.start()
                         .compare(left.getName(), right.getName())
                         .compare(left.getReturnType(), right.getReturnType(), CLASS_ORDERING)
                         .compare(
                                 Arrays.asList(left.getParameterTypes()),
                                 Arrays.asList(right.getParameterTypes()),
                                 CLASS_ORDERING.lexicographical())
                         .result();
              }
           };

   /**
    * Returns true if the method is eligible to be injected. This is different than {@link
    * #isValidMethod}, because ineligibility will not drop a method from being injected if a
    * superclass was eligible & valid. Bridge & synthetic methods are excluded from eligibility for
    * two reasons:
    *
    * <p>Prior to Java8, javac would generate these methods in subclasses without annotations, which
    * means this would accidentally stop injecting a method annotated with {@link
    * javax.inject.Inject}, since the spec says to stop injecting if a subclass isn't annotated with
    * it.
    *
    * <p>Starting at Java8, javac copies the annotations to the generated subclass method, except it
    * leaves out the generic types. If this considered it a valid injectable method, this would eject
    * the parent's overridden method that had the proper generic types, and would use invalid
    * injectable parameters as a result.
    *
    * <p>The fix for both is simply to ignore these synthetic bridge methods.
    */
   private static boolean isEligibleForInjection(Method method, boolean statics) {
      return Modifier.isStatic(method.getModifiers()) == statics
              && !method.isBridge()
              && !method.isSynthetic();
   }

   private static boolean isValidMethod(InjectableMethod injectableMethod, Errors errors) {
      boolean result = true;
      if (injectableMethod.jsr330) {
         Method method = injectableMethod.method;
         if (Modifier.isAbstract(method.getModifiers())) {
            errors.cannotInjectAbstractMethod(method);
            result = false;
         }
         if (method.getTypeParameters().length > 0) {
            errors.cannotInjectMethodWithTypeParameters(method);
            result = false;
         }
      }
      return result;
   }

   private static List<TypeLiteral<?>> hierarchyFor(TypeLiteral<?> type) {
      List<TypeLiteral<?>> hierarchy = new ArrayList<>();
      TypeLiteral<?> current = type;
      while (current.getRawType() != Object.class) {
         hierarchy.add(current);
         current = current.getSupertype(current.getRawType().getSuperclass());
      }
      return hierarchy;
   }

   /**
    * Returns true if a overrides b. Assumes signatures of a and b are the same and a's declaring
    * class is a subclass of b's declaring class.
    */
   private static boolean overrides(Method a, Method b) {
      // See JLS section 8.4.8.1
      int modifiers = b.getModifiers();
      if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
         return true;
      }
      if (Modifier.isPrivate(modifiers)) {
         return false;
      }
      // b must be package-private
      return a.getDeclaringClass().getPackage().equals(b.getDeclaringClass().getPackage());
   }

   /** A method signature. Used to handle method overridding. */
   static class Signature {

      final String name;
      final Class[] parameterTypes;
      final int hash;

      Signature(Method method) {
         this.name = method.getName();
         this.parameterTypes = method.getParameterTypes();

         int h = name.hashCode();
         h = h * 31 + parameterTypes.length;
         for (Class parameterType : parameterTypes) {
            h = h * 31 + parameterType.hashCode();
         }
         this.hash = h;
      }

      @Override
      public int hashCode() {
         return this.hash;
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof Signature)) {
            return false;
         }

         Signature other = (Signature) o;
         if (!name.equals(other.name)) {
            return false;
         }

         if (parameterTypes.length != other.parameterTypes.length) {
            return false;
         }

         for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] != other.parameterTypes[i]) {
               return false;
            }
         }

         return true;
      }
   }
}
