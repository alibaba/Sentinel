package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.SourceProvider;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.Message;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;

public final class InternalProvisionException extends Exception {
   private static final Logger logger = Logger.getLogger(Guice.class.getName());
   private static final Set<com.google.inject.spi.Dependency<?>> warnedDependencies = Collections.newSetFromMap(new ConcurrentHashMap());
   private final List<Object> sourcesToPrepend;
   private final ImmutableList<Message> errors;

   public static InternalProvisionException circularDependenciesDisabled(Class<?> expectedType) {
      return create("Found a circular dependency involving %s, and circular dependencies are disabled.", expectedType);
   }

   public static InternalProvisionException cannotProxyClass(Class<?> expectedType) {
      return create("Tried proxying %s to support a circular dependency, but it is not an interface.", expectedType);
   }

   public static InternalProvisionException create(String format, Object... arguments) {
      return new InternalProvisionException(Messages.create(format, arguments));
   }

   public static InternalProvisionException errorInUserCode(Throwable cause, String messageFormat, Object... arguments) {
      Collection<Message> messages = Errors.getMessagesFromThrowable(cause);
      return !messages.isEmpty() ? new InternalProvisionException(messages) : new InternalProvisionException(Messages.create(cause, messageFormat, arguments));
   }

   public static InternalProvisionException subtypeNotProvided(Class<? extends Provider<?>> providerType, Class<?> type) {
      return create("%s doesn't provide instances of %s.", providerType, type);
   }

   public static InternalProvisionException errorInProvider(Throwable cause) {
      return errorInUserCode(cause, "Error in custom provider, %s", cause);
   }

   public static InternalProvisionException errorInjectingMethod(Throwable cause) {
      return errorInUserCode(cause, "Error injecting method, %s", cause);
   }

   public static InternalProvisionException errorInjectingConstructor(Throwable cause) {
      return errorInUserCode(cause, "Error injecting constructor, %s", cause);
   }

   public static InternalProvisionException errorInUserInjector(MembersInjector<?> listener, TypeLiteral<?> type, RuntimeException cause) {
      return errorInUserCode(cause, "Error injecting %s using %s.%n Reason: %s", type, listener, cause);
   }

   public static InternalProvisionException jitDisabled(Key<?> key) {
      return create("Explicit bindings are required and %s is not explicitly bound.", key);
   }

   public static InternalProvisionException errorNotifyingInjectionListener(InjectionListener<?> listener, TypeLiteral<?> type, RuntimeException cause) {
      return errorInUserCode(cause, "Error notifying InjectionListener %s of %s.%n Reason: %s", listener, type, cause);
   }

   static void onNullInjectedIntoNonNullableDependency(Object source, com.google.inject.spi.Dependency<?> dependency) throws InternalProvisionException {
      if (dependency.getInjectionPoint().getMember() instanceof Method) {
         Method annotated = (Method)dependency.getInjectionPoint().getMember();
         if (annotated.isAnnotationPresent(Provides.class)) {
            switch(InternalFlags.getNullableProvidesOption()) {
            case ERROR:
            default:
               break;
            case IGNORE:
               return;
            case WARN:
               if (warnedDependencies.add(dependency)) {
                  logger.log(Level.WARNING, "Guice injected null into {0} (a {1}), please mark it @Nullable. Use -Dguice_check_nullable_provides_params=ERROR to turn this into an error.", new Object[]{Messages.formatParameter(dependency), Messages.convert(dependency.getKey())});
               }

               return;
            }
         }
      }

      Object formattedDependency = dependency.getParameterIndex() != -1 ? Messages.formatParameter(dependency) : StackTraceElements.forMember(dependency.getInjectionPoint().getMember());
      throw create("null returned by binding at %s%n but %s is not @Nullable", source, formattedDependency).addSource(source);
   }

   private InternalProvisionException(Message error) {
      this((Iterable)ImmutableList.of(error));
   }

   private InternalProvisionException(Iterable<Message> errors) {
      this.sourcesToPrepend = new ArrayList();
      this.errors = ImmutableList.copyOf(errors);
      Preconditions.checkArgument(!this.errors.isEmpty(), "Can't create a provision exception with no errors");
   }

   InternalProvisionException addSource(Object source) {
      if (source == SourceProvider.UNKNOWN_SOURCE) {
         return this;
      } else {
         int sz = this.sourcesToPrepend.size();
         if (sz > 0 && this.sourcesToPrepend.get(sz - 1) == source) {
            return this;
         } else {
            this.sourcesToPrepend.add(source);
            return this;
         }
      }
   }

   ImmutableList<Message> getErrors() {
      Builder<Message> builder = ImmutableList.builder();
      List<Object> newSources = Lists.reverse(this.sourcesToPrepend);
      Iterator i$ = this.errors.iterator();

      while(i$.hasNext()) {
         Message error = (Message)i$.next();
         builder.add(Messages.mergeSources(newSources, error));
      }

      return builder.build();
   }

   public ProvisionException toProvisionException() {
      return new ProvisionException(this.getErrors());
   }
}
