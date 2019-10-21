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

package com.google.inject.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.Message;
import com.google.inject.spi.ModuleAnnotatedMethodScanner;
import com.google.inject.util.Modules;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Creates bindings to methods annotated with {@literal @}{@link Provides}. Use the scope and
 * binding annotations on the provider method to configure the binding.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 */
public final class ProviderMethodsModule implements Module {
   private final Object delegate;
   private final TypeLiteral<?> typeLiteral;
   private final boolean skipFastClassGeneration;
   private final ModuleAnnotatedMethodScanner scanner;

   private ProviderMethodsModule(
           Object delegate, boolean skipFastClassGeneration, ModuleAnnotatedMethodScanner scanner) {
      this.delegate = checkNotNull(delegate, "delegate");
      this.typeLiteral = TypeLiteral.get(this.delegate.getClass());
      this.skipFastClassGeneration = skipFastClassGeneration;
      this.scanner = scanner;
   }

   /** Returns a module which creates bindings for provider methods from the given module. */
   public static Module forModule(Module module) {
      return forObject(module, false, ProvidesMethodScanner.INSTANCE);
   }

   /** Returns a module which creates bindings methods in the module that match the scanner. */
   public static Module forModule(Object module, ModuleAnnotatedMethodScanner scanner) {
      return forObject(module, false, scanner);
   }

   /**
    * Returns a module which creates bindings for provider methods from the given object. This is
    * useful notably for <a href="http://code.google.com/p/google-gin/">GIN</a>
    *
    * <p>This will skip bytecode generation for provider methods, since it is assumed that callers
    * are only interested in Module metadata.
    */
   public static Module forObject(Object object) {
      return forObject(object, true, ProvidesMethodScanner.INSTANCE);
   }

   private static Module forObject(
           Object object, boolean skipFastClassGeneration, ModuleAnnotatedMethodScanner scanner) {
      // avoid infinite recursion, since installing a module always installs itself
      if (object instanceof ProviderMethodsModule) {
         return Modules.EMPTY_MODULE;
      }

      return new ProviderMethodsModule(object, skipFastClassGeneration, scanner);
   }

   public Object getDelegateModule() {
      return delegate;
   }

   @Override
   public void configure(Binder binder) {
      for (ProviderMethod<?> providerMethod : getProviderMethods(binder)) {
         providerMethod.configure(binder);
      }
   }

   public List<ProviderMethod<?>> getProviderMethods(Binder binder) {
      List<ProviderMethod<?>> result = null;
      // The highest class in the type hierarchy that contained a provider method definition.
      Class<?> superMostClass = delegate.getClass();
      for (Class<?> c = delegate.getClass(); c != Object.class; c = c.getSuperclass()) {
         for (Method method : c.getDeclaredMethods()) {
            Annotation annotation = getAnnotation(binder, method);
            if (annotation != null) {
               if (result == null) {
                  result = Lists.newArrayList();
               }
               result.add(createProviderMethod(binder, method, annotation));
               superMostClass = c;
            }
         }
      }
      if (result == null) {
         // We didn't find anything
         return ImmutableList.of();
      }
      // We have found some provider methods, now we need to check if any were overridden.
      // We do this as a separate pass to avoid calculating all the signatures when there are no
      // provides methods, or when all provides methods are defined in a single class.
      Multimap<Signature, Method> methodsBySignature = null;
      // We can stop scanning when we see superMostClass, since no superclass method can override
      // a method in a subclass.  Corrollary, if superMostClass == delegate.getClass(), there can be
      // no overrides of a provides method.
      for (Class<?> c = delegate.getClass(); c != superMostClass; c = c.getSuperclass()) {
         for (Method method : c.getDeclaredMethods()) {
            if (((method.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC)) == 0)
                    && !method.isBridge()
                    && !method.isSynthetic()) {
               if (methodsBySignature == null) {
                  methodsBySignature = HashMultimap.create();
               }
               methodsBySignature.put(new Signature(typeLiteral, method), method);
            }
         }
      }
      if (methodsBySignature != null) {
         // we have found all the signatures and now need to identify if any were overridden
         // In the worst case this will have O(n^2) in the number of @Provides methods, but that is
         // only assuming that every method is an override, in general it should be very quick.
         for (ProviderMethod<?> provider : result) {
            Method method = provider.getMethod();
            for (Method matchingSignature :
                    methodsBySignature.get(new Signature(typeLiteral, method))) {
               // matching signature is in the same class or a super class, therefore method cannot be
               // overridding it.
               if (matchingSignature.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
                  continue;
               }
               // now we know matching signature is in a subtype of method.getDeclaringClass()
               if (overrides(matchingSignature, method)) {
                  String annotationString =
                          provider.getAnnotation().annotationType() == Provides.class
                                  ? "@Provides"
                                  : "@" + provider.getAnnotation().annotationType().getCanonicalName();
                  binder.addError(
                          "Overriding "
                                  + annotationString
                                  + " methods is not allowed."
                                  + "\n\t"
                                  + annotationString
                                  + " method: %s\n\toverridden by: %s",
                          method,
                          matchingSignature);
                  break;
               }
            }
         }
      }
      return result;
   }

   /** Returns the annotation that is claimed by the scanner, or null if there is none. */
   private Annotation getAnnotation(Binder binder, Method method) {
      if (method.isBridge() || method.isSynthetic()) {
         return null;
      }
      Annotation annotation = null;
      for (Class<? extends Annotation> annotationClass : scanner.annotationClasses()) {
         Annotation foundAnnotation = method.getAnnotation(annotationClass);
         if (foundAnnotation != null) {
            if (annotation != null) {
               binder.addError(
                       "More than one annotation claimed by %s on method %s."
                               + " Methods can only have one annotation claimed per scanner.",
                       scanner, method);
               return null;
            }
            annotation = foundAnnotation;
         }
      }
      return annotation;
   }

   private static final class Signature {
      final Class<?>[] parameters;
      final String name;
      final int hashCode;

      Signature(TypeLiteral<?> typeLiteral, Method method) {
         this.name = method.getName();
         // We need to 'resolve' the parameters against the actual class type in case this method uses
         // type parameters.  This is so we can detect overrides of generic superclass methods where
         // the subclass specifies the type parameter.  javac implements these kinds of overrides via
         // bridge methods, but we don't want to give errors on bridge methods (but rather the target
         // of the bridge).
         List<TypeLiteral<?>> resolvedParameterTypes = typeLiteral.getParameterTypes(method);
         this.parameters = new Class<?>[resolvedParameterTypes.size()];
         int i = 0;
         for (TypeLiteral<?> type : resolvedParameterTypes) {
            parameters[i] = type.getRawType();
         }
         this.hashCode = name.hashCode() + 31 * Arrays.hashCode(parameters);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof Signature) {
            Signature other = (Signature) obj;
            return other.name.equals(name) && Arrays.equals(parameters, other.parameters);
         }
         return false;
      }

      @Override
      public int hashCode() {
         return hashCode;
      }
   }

   /** Returns true if a overrides b, assumes that the signatures match */
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

   private <T> ProviderMethod<T> createProviderMethod(
           Binder binder, Method method, Annotation annotation) {
      binder = binder.withSource(method);
      Errors errors = new Errors(method);

      // prepare the parameter providers
      InjectionPoint point = InjectionPoint.forMethod(method, typeLiteral);
      @SuppressWarnings("unchecked") // Define T as the method's return type.
              TypeLiteral<T> returnType = (TypeLiteral<T>) typeLiteral.getReturnType(method);
      Key<T> key = getKey(errors, returnType, method, method.getAnnotations());
      try {
         key = scanner.prepareMethod(binder, annotation, key, point);
      } catch (Throwable t) {
         binder.addError(t);
      }
      Class<? extends Annotation> scopeAnnotation =
              Annotations.findScopeAnnotation(errors, method.getAnnotations());
      for (Message message : errors.getMessages()) {
         binder.addError(message);
      }
      return ProviderMethod.create(
              key,
              method,
              delegate,
              ImmutableSet.copyOf(point.getDependencies()),
              scopeAnnotation,
              skipFastClassGeneration,
              annotation);
   }

   <T> Key<T> getKey(Errors errors, TypeLiteral<T> type, Member member, Annotation[] annotations) {
      Annotation bindingAnnotation = Annotations.findBindingAnnotation(errors, member, annotations);
      return bindingAnnotation == null ? Key.get(type) : Key.get(type, bindingAnnotation);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof ProviderMethodsModule
              && ((ProviderMethodsModule) o).delegate == delegate
              && ((ProviderMethodsModule) o).scanner == scanner;
   }

   @Override
   public int hashCode() {
      return delegate.hashCode();
   }
}
