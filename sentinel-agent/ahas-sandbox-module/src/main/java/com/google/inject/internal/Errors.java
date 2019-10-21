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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Primitives;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.SourceProvider;
import com.google.inject.spi.ElementSource;
import com.google.inject.spi.Message;
import com.google.inject.spi.ScopeBinding;
import com.google.inject.spi.TypeConverterBinding;
import com.google.inject.spi.TypeListenerBinding;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of error messages. If this type is passed as a method parameter, the method is
 * considered to have executed successfully only if new errors were not added to this collection.
 *
 * <p>Errors can be chained to provide additional context. To add context, call {@link #withSource}
 * to create a new Errors instance that contains additional context. All messages added to the
 * returned instance will contain full context.
 *
 * <p>To avoid messages with redundant context, {@link #withSource} should be added sparingly. A
 * good rule of thumb is to assume a method's caller has already specified enough context to
 * identify that method. When calling a method that's defined in a different context, call that
 * method with an errors object that includes its context.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 */
public final class Errors implements Serializable {

   /** When a binding is not found, show at most this many bindings with the same type */
   private static final int MAX_MATCHING_TYPES_REPORTED = 3;

   /** When a binding is not found, show at most this many bindings that have some similarities */
   private static final int MAX_RELATED_TYPES_REPORTED = 3;

   /**
    * Throws a ConfigurationException with an NullPointerExceptions as the cause if the given
    * reference is {@code null}.
    */
   static <T> T checkNotNull(T reference, String name) {
      if (reference != null) {
         return reference;
      }

      NullPointerException npe = new NullPointerException(name);
      throw new ConfigurationException(ImmutableSet.of(new Message(npe.toString(), npe)));
   }

   /**
    * Throws a ConfigurationException with a formatted {@link Message} if this condition is {@code
    * false}.
    */
   static void checkConfiguration(boolean condition, String format, Object... args) {
      if (condition) {
         return;
      }

      throw new ConfigurationException(ImmutableSet.of(new Message(Errors.format(format, args))));
   }

   /**
    * If the key is unknown and it is one of these types, it generally means there is a missing
    * annotation.
    */
   private static final ImmutableSet<Class<?>> COMMON_AMBIGUOUS_TYPES =
           ImmutableSet.<Class<?>>builder()
                   .add(Object.class)
                   .add(String.class)
                   .addAll(Primitives.allWrapperTypes())
                   .build();

   /** The root errors object. Used to access the list of error messages. */
   private final Errors root;

   /** The parent errors object. Used to obtain the chain of source objects. */
   private final Errors parent;

   /** The leaf source for errors added here. */
   private final Object source;

   /** null unless (root == this) and error messages exist. Never an empty list. */
   private List<Message> errors; // lazy, use getErrorsForAdd()

   public Errors() {
      this.root = this;
      this.parent = null;
      this.source = SourceProvider.UNKNOWN_SOURCE;
   }

   public Errors(Object source) {
      this.root = this;
      this.parent = null;
      this.source = source;
   }

   private Errors(Errors parent, Object source) {
      this.root = parent.root;
      this.parent = parent;
      this.source = source;
   }

   /** Returns an instance that uses {@code source} as a reference point for newly added errors. */
   public Errors withSource(Object source) {
      return source == this.source || source == SourceProvider.UNKNOWN_SOURCE
              ? this
              : new Errors(this, source);
   }

   /**
    * We use a fairly generic error message here. The motivation is to share the same message for
    * both bind time errors:
    *
    * <pre><code>Guice.createInjector(new AbstractModule() {
    *   public void configure() {
    *     bind(Runnable.class);
    *   }
    * }</code></pre>
    *
    * ...and at provide-time errors:
    *
    * <pre><code>Guice.createInjector().getInstance(Runnable.class);</code></pre>
    *
    * Otherwise we need to know who's calling when resolving a just-in-time binding, which makes
    * things unnecessarily complex.
    */
   public Errors missingImplementation(Key key) {
      return addMessage("No implementation for %s was bound.", key);
   }

   /** Within guice's core, allow for better missing binding messages */
   <T> Errors missingImplementationWithHint(Key<T> key, Injector injector) {
      StringBuilder sb = new StringBuilder();

      sb.append(format("No implementation for %s was bound.", key));

      // Keys which have similar strings as the desired key
      List<String> possibleMatches = new ArrayList<>();

      // Check for other keys that may have the same type,
      // but not the same annotation
      TypeLiteral<T> type = key.getTypeLiteral();
      List<Binding<T>> sameTypes = injector.findBindingsByType(type);
      if (!sameTypes.isEmpty()) {
         sb.append(format("%n  Did you mean?"));
         int howMany = Math.min(sameTypes.size(), MAX_MATCHING_TYPES_REPORTED);
         for (int i = 0; i < howMany; ++i) {
            // TODO: Look into a better way to prioritize suggestions. For example, possbily
            // use levenshtein distance of the given annotation vs actual annotation.
            sb.append(format("%n    * %s", sameTypes.get(i).getKey()));
         }
         int remaining = sameTypes.size() - MAX_MATCHING_TYPES_REPORTED;
         if (remaining > 0) {
            String plural = (remaining == 1) ? "" : "s";
            sb.append(format("%n    %d more binding%s with other annotations.", remaining, plural));
         }
      } else {
         // For now, do a simple substring search for possibilities. This can help spot
         // issues when there are generics being used (such as a wrapper class) and the
         // user has forgotten they need to bind based on the wrapper, not the underlying
         // class. In the future, consider doing a strict in-depth type search.
         // TODO: Look into a better way to prioritize suggestions. For example, possbily
         // use levenshtein distance of the type literal strings.
         String want = type.toString();
         Map<Key<?>, Binding<?>> bindingMap = injector.getAllBindings();
         for (Key<?> bindingKey : bindingMap.keySet()) {
            String have = bindingKey.getTypeLiteral().toString();
            if (have.contains(want) || want.contains(have)) {
               Formatter fmt = new Formatter();
               Messages.formatSource(fmt, bindingMap.get(bindingKey).getSource());
               String match = String.format("%s bound%s", convert(bindingKey), fmt.toString());
               possibleMatches.add(match);
               // TODO: Consider a check that if there are more than some number of results,
               // don't suggest any.
               if (possibleMatches.size() > MAX_RELATED_TYPES_REPORTED) {
                  // Early exit if we have found more than we need.
                  break;
               }
            }
         }

         if ((possibleMatches.size() > 0) && (possibleMatches.size() <= MAX_RELATED_TYPES_REPORTED)) {
            sb.append(format("%n  Did you mean?"));
            for (String possibleMatch : possibleMatches) {
               sb.append(format("%n    %s", possibleMatch));
            }
         }
      }

      // If where are no possibilities to suggest, then handle the case of missing
      // annotations on simple types. This is usually a bad idea.
      if (sameTypes.isEmpty()
              && possibleMatches.isEmpty()
              && key.getAnnotation() == null
              && COMMON_AMBIGUOUS_TYPES.contains(key.getTypeLiteral().getRawType())) {
         // We don't recommend using such simple types without annotations.
         sb.append(format("%nThe key seems very generic, did you forget an annotation?"));
      }

      return addMessage(sb.toString());
   }

   public Errors jitDisabled(Key<?> key) {
      return addMessage("Explicit bindings are required and %s is not explicitly bound.", key);
   }

   public Errors jitDisabledInParent(Key<?> key) {
      return addMessage(
              "Explicit bindings are required and %s would be bound in a parent injector.%n"
                      + "Please add an explicit binding for it, either in the child or the parent.",
              key);
   }

   public Errors atInjectRequired(Class clazz) {
      return addMessage(
              "Explicit @Inject annotations are required on constructors,"
                      + " but %s has no constructors annotated with @Inject.",
              clazz);
   }

   public Errors converterReturnedNull(
           String stringValue,
           Object source,
           TypeLiteral<?> type,
           TypeConverterBinding typeConverterBinding) {
      return addMessage(
              "Received null converting '%s' (bound at %s) to %s%n using %s.",
              stringValue, convert(source), type, typeConverterBinding);
   }

   public Errors conversionTypeError(
           String stringValue,
           Object source,
           TypeLiteral<?> type,
           TypeConverterBinding typeConverterBinding,
           Object converted) {
      return addMessage(
              "Type mismatch converting '%s' (bound at %s) to %s%n"
                      + " using %s.%n"
                      + " Converter returned %s.",
              stringValue, convert(source), type, typeConverterBinding, converted);
   }

   public Errors conversionError(
           String stringValue,
           Object source,
           TypeLiteral<?> type,
           TypeConverterBinding typeConverterBinding,
           RuntimeException cause) {
      return errorInUserCode(
              cause,
              "Error converting '%s' (bound at %s) to %s%n using %s.%n Reason: %s",
              stringValue,
              convert(source),
              type,
              typeConverterBinding,
              cause);
   }

   public Errors ambiguousTypeConversion(
           String stringValue,
           Object source,
           TypeLiteral<?> type,
           TypeConverterBinding a,
           TypeConverterBinding b) {
      return addMessage(
              "Multiple converters can convert '%s' (bound at %s) to %s:%n"
                      + " %s and%n"
                      + " %s.%n"
                      + " Please adjust your type converter configuration to avoid overlapping matches.",
              stringValue, convert(source), type, a, b);
   }

   public Errors bindingToProvider() {
      return addMessage("Binding to Provider is not allowed.");
   }

   public Errors notASubtype(Class<?> implementationType, Class<?> type) {
      return addMessage("%s doesn't extend %s.", implementationType, type);
   }

   public Errors recursiveImplementationType() {
      return addMessage("@ImplementedBy points to the same class it annotates.");
   }

   public Errors recursiveProviderType() {
      return addMessage("@ProvidedBy points to the same class it annotates.");
   }

   public Errors missingRuntimeRetention(Class<? extends Annotation> annotation) {
      return addMessage(format("Please annotate %s with @Retention(RUNTIME).", annotation));
   }

   public Errors missingScopeAnnotation(Class<? extends Annotation> annotation) {
      return addMessage(format("Please annotate %s with @ScopeAnnotation.", annotation));
   }

   public Errors optionalConstructor(Constructor constructor) {
      return addMessage(
              "%s is annotated @Inject(optional=true), but constructors cannot be optional.",
              constructor);
   }

   public Errors cannotBindToGuiceType(String simpleName) {
      return addMessage("Binding to core guice framework type is not allowed: %s.", simpleName);
   }

   public Errors scopeNotFound(Class<? extends Annotation> scopeAnnotation) {
      return addMessage("No scope is bound to %s.", scopeAnnotation);
   }

   public Errors scopeAnnotationOnAbstractType(
           Class<? extends Annotation> scopeAnnotation, Class<?> type, Object source) {
      return addMessage(
              "%s is annotated with %s, but scope annotations are not supported "
                      + "for abstract types.%n Bound at %s.",
              type, scopeAnnotation, convert(source));
   }

   public Errors misplacedBindingAnnotation(Member member, Annotation bindingAnnotation) {
      return addMessage(
              "%s is annotated with %s, but binding annotations should be applied "
                      + "to its parameters instead.",
              member, bindingAnnotation);
   }

   private static final String CONSTRUCTOR_RULES =
           "Classes must have either one (and only one) constructor "
                   + "annotated with @Inject or a zero-argument constructor that is not private.";

   public Errors missingConstructor(Class<?> implementation) {
      return addMessage(
              "Could not find a suitable constructor in %s. " + CONSTRUCTOR_RULES, implementation);
   }

   public Errors tooManyConstructors(Class<?> implementation) {
      return addMessage(
              "%s has more than one constructor annotated with @Inject. " + CONSTRUCTOR_RULES,
              implementation);
   }

   public Errors constructorNotDefinedByType(Constructor<?> constructor, TypeLiteral<?> type) {
      return addMessage("%s does not define %s", type, constructor);
   }

   public Errors duplicateScopes(
           ScopeBinding existing, Class<? extends Annotation> annotationType, Scope scope) {
      return addMessage(
              "Scope %s is already bound to %s at %s.%n Cannot bind %s.",
              existing.getScope(), annotationType, existing.getSource(), scope);
   }

   public Errors voidProviderMethod() {
      return addMessage("Provider methods must return a value. Do not return void.");
   }

   public Errors missingConstantValues() {
      return addMessage("Missing constant value. Please call to(...).");
   }

   public Errors cannotInjectInnerClass(Class<?> type) {
      return addMessage(
              "Injecting into inner classes is not supported.  "
                      + "Please use a 'static' class (top-level or nested) instead of %s.",
              type);
   }

   public Errors duplicateBindingAnnotations(
           Member member, Class<? extends Annotation> a, Class<? extends Annotation> b) {
      return addMessage(
              "%s has more than one annotation annotated with @BindingAnnotation: %s and %s",
              member, a, b);
   }

   public Errors staticInjectionOnInterface(Class<?> clazz) {
      return addMessage("%s is an interface, but interfaces have no static injection points.", clazz);
   }

   public Errors cannotInjectFinalField(Field field) {
      return addMessage("Injected field %s cannot be final.", field);
   }

   public Errors cannotInjectAbstractMethod(Method method) {
      return addMessage("Injected method %s cannot be abstract.", method);
   }

   public Errors cannotInjectNonVoidMethod(Method method) {
      return addMessage("Injected method %s must return void.", method);
   }

   public Errors cannotInjectMethodWithTypeParameters(Method method) {
      return addMessage("Injected method %s cannot declare type parameters of its own.", method);
   }

   public Errors duplicateScopeAnnotations(
           Class<? extends Annotation> a, Class<? extends Annotation> b) {
      return addMessage("More than one scope annotation was found: %s and %s.", a, b);
   }

   public Errors recursiveBinding() {
      return addMessage("Binding points to itself.");
   }

   public Errors bindingAlreadySet(Key<?> key, Object source) {
      return addMessage("A binding to %s was already configured at %s.", key, convert(source));
   }

   public Errors jitBindingAlreadySet(Key<?> key) {
      return addMessage(
              "A just-in-time binding to %s was already configured on a parent injector.", key);
   }

   public Errors childBindingAlreadySet(Key<?> key, Set<Object> sources) {
      Formatter allSources = new Formatter();
      for (Object source : sources) {
         if (source == null) {
            allSources.format("%n    (bound by a just-in-time binding)");
         } else {
            allSources.format("%n    bound at %s", source);
         }
      }
      Errors errors =
              addMessage(
                      "Unable to create binding for %s."
                              + " It was already configured on one or more child injectors or private modules"
                              + "%s%n"
                              + "  If it was in a PrivateModule, did you forget to expose the binding?",
                      key, allSources.out());
      return errors;
   }

   public Errors errorCheckingDuplicateBinding(Key<?> key, Object source, Throwable t) {
      return addMessage(
              "A binding to %s was already configured at %s and an error was thrown "
                      + "while checking duplicate bindings.  Error: %s",
              key, convert(source), t);
   }

   public Errors errorNotifyingTypeListener(
           TypeListenerBinding listener, TypeLiteral<?> type, Throwable cause) {
      return errorInUserCode(
              cause,
              "Error notifying TypeListener %s (bound at %s) of %s.%n Reason: %s",
              listener.getListener(),
              convert(listener.getSource()),
              type,
              cause);
   }

   public Errors exposedButNotBound(Key<?> key) {
      return addMessage("Could not expose() %s, it must be explicitly bound.", key);
   }

   public Errors keyNotFullySpecified(TypeLiteral<?> typeLiteral) {
      return addMessage("%s cannot be used as a key; It is not fully specified.", typeLiteral);
   }

   public Errors errorEnhancingClass(Class<?> clazz, Throwable cause) {
      return errorInUserCode(cause, "Unable to method intercept: %s", clazz);
   }

   public static Collection<Message> getMessagesFromThrowable(Throwable throwable) {
      if (throwable instanceof ProvisionException) {
         return ((ProvisionException) throwable).getErrorMessages();
      } else if (throwable instanceof ConfigurationException) {
         return ((ConfigurationException) throwable).getErrorMessages();
      } else if (throwable instanceof CreationException) {
         return ((CreationException) throwable).getErrorMessages();
      } else {
         return ImmutableSet.of();
      }
   }

   public Errors errorInUserCode(Throwable cause, String messageFormat, Object... arguments) {
      Collection<Message> messages = getMessagesFromThrowable(cause);

      if (!messages.isEmpty()) {
         return merge(messages);
      } else {
         return addMessage(cause, messageFormat, arguments);
      }
   }

   public Errors cannotInjectRawProvider() {
      return addMessage("Cannot inject a Provider that has no type parameter");
   }

   public Errors cannotInjectRawMembersInjector() {
      return addMessage("Cannot inject a MembersInjector that has no type parameter");
   }

   public Errors cannotInjectTypeLiteralOf(Type unsupportedType) {
      return addMessage("Cannot inject a TypeLiteral of %s", unsupportedType);
   }

   public Errors cannotInjectRawTypeLiteral() {
      return addMessage("Cannot inject a TypeLiteral that has no type parameter");
   }

   public void throwCreationExceptionIfErrorsExist() {
      if (!hasErrors()) {
         return;
      }

      throw new CreationException(getMessages());
   }

   public void throwConfigurationExceptionIfErrorsExist() {
      if (!hasErrors()) {
         return;
      }

      throw new ConfigurationException(getMessages());
   }

   // Guice no longer calls this, but external callers do
   public void throwProvisionExceptionIfErrorsExist() {
      if (!hasErrors()) {
         return;
      }

      throw new ProvisionException(getMessages());
   }

   public Errors merge(Collection<Message> messages) {
      List<Object> sources = getSources();
      for (Message message : messages) {
         addMessage(Messages.mergeSources(sources, message));
      }
      return this;
   }

   public Errors merge(Errors moreErrors) {
      if (moreErrors.root == root || moreErrors.root.errors == null) {
         return this;
      }

      merge(moreErrors.root.errors);
      return this;
   }

   public Errors merge(InternalProvisionException ipe) {
      merge(ipe.getErrors());
      return this;
   }

   private List<Object> getSources() {
      List<Object> sources = Lists.newArrayList();
      for (Errors e = this; e != null; e = e.parent) {
         if (e.source != SourceProvider.UNKNOWN_SOURCE) {
            sources.add(0, e.source);
         }
      }
      return sources;
   }

   public void throwIfNewErrors(int expectedSize) throws ErrorsException {
      if (size() == expectedSize) {
         return;
      }

      throw toException();
   }

   public ErrorsException toException() {
      return new ErrorsException(this);
   }

   public boolean hasErrors() {
      return root.errors != null;
   }

   public Errors addMessage(String messageFormat, Object... arguments) {
      return addMessage(null, messageFormat, arguments);
   }

   private Errors addMessage(Throwable cause, String messageFormat, Object... arguments) {
      addMessage(Messages.create(cause, getSources(), messageFormat, arguments));
      return this;
   }

   public Errors addMessage(Message message) {
      if (root.errors == null) {
         root.errors = Lists.newArrayList();
      }
      root.errors.add(message);
      return this;
   }

   // TODO(lukes): inline into callers
   public static String format(String messageFormat, Object... arguments) {
      return Messages.format(messageFormat, arguments);
   }

   public List<Message> getMessages() {
      if (root.errors == null) {
         return ImmutableList.of();
      }

      return new Ordering<Message>() {
         @Override
         public int compare(Message a, Message b) {
            return a.getSource().compareTo(b.getSource());
         }
      }.sortedCopy(root.errors);
   }

   public int size() {
      return root.errors == null ? 0 : root.errors.size();
   }

   // TODO(lukes): inline in callers.  There are some callers outside of guice, so this is difficult
   public static Object convert(Object o) {
      return Messages.convert(o);
   }

   // TODO(lukes): inline in callers.  There are some callers outside of guice, so this is difficult
   public static Object convert(Object o, ElementSource source) {
      return Messages.convert(o, source);
   }

   // TODO(lukes): inline in callers.  There are some callers outside of guice, so this is difficult
   public static void formatSource(Formatter formatter, Object source) {
      Messages.formatSource(formatter, source);
   }

}
