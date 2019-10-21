package com.google.inject;

import com.google.common.base.Preconditions;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.AnnotatedElementBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeConverter;

import java.lang.annotation.Annotation;

public abstract class PrivateModule implements com.google.inject.Module {
   private PrivateBinder binder;

   public final synchronized void configure(Binder binder) {
      Preconditions.checkState(this.binder == null, "Re-entry is not allowed.");
      this.binder = (PrivateBinder)binder.skipSources(PrivateModule.class);

      try {
         this.configure();
      } finally {
         this.binder = null;
      }

   }

   protected abstract void configure();

   protected final <T> void expose(Key<T> key) {
      this.binder().expose(key);
   }

   protected final AnnotatedElementBuilder expose(Class<?> type) {
      return this.binder().expose(type);
   }

   protected final AnnotatedElementBuilder expose(TypeLiteral<?> type) {
      return this.binder().expose(type);
   }

   protected final PrivateBinder binder() {
      Preconditions.checkState(this.binder != null, "The binder can only be used inside configure()");
      return this.binder;
   }

   protected final void bindScope(Class<? extends Annotation> scopeAnnotation, Scope scope) {
      this.binder().bindScope(scopeAnnotation, scope);
   }

   protected final <T> LinkedBindingBuilder<T> bind(Key<T> key) {
      return this.binder().bind(key);
   }

   protected final <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
      return this.binder().bind(typeLiteral);
   }

   protected final <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
      return this.binder().bind(clazz);
   }

   protected final AnnotatedConstantBindingBuilder bindConstant() {
      return this.binder().bindConstant();
   }

   protected final void install(Module module) {
      this.binder().install(module);
   }

   protected final void addError(String message, Object... arguments) {
      this.binder().addError(message, arguments);
   }

   protected final void addError(Throwable t) {
      this.binder().addError(t);
   }

   protected final void addError(Message message) {
      this.binder().addError(message);
   }

   protected final void requestInjection(Object instance) {
      this.binder().requestInjection(instance);
   }

   protected final void requestStaticInjection(Class... types) {
      this.binder().requestStaticInjection(types);
   }

   protected final void requireBinding(Key<?> key) {
      this.binder().getProvider(key);
   }

   protected final void requireBinding(Class<?> type) {
      this.binder().getProvider(type);
   }

   protected final <T> Provider<T> getProvider(Key<T> key) {
      return this.binder().getProvider(key);
   }

   protected final <T> Provider<T> getProvider(Class<T> type) {
      return this.binder().getProvider(type);
   }

   protected final void convertToTypes(com.google.inject.matcher.Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
      this.binder().convertToTypes(typeMatcher, converter);
   }

   protected final Stage currentStage() {
      return this.binder().currentStage();
   }

   protected <T> MembersInjector<T> getMembersInjector(Class<T> type) {
      return this.binder().getMembersInjector(type);
   }

   protected <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> type) {
      return this.binder().getMembersInjector(type);
   }

   protected void bindListener(com.google.inject.matcher.Matcher<? super TypeLiteral<?>> typeMatcher, com.google.inject.spi.TypeListener listener) {
      this.binder().bindListener(typeMatcher, listener);
   }

   protected void bindListener(com.google.inject.matcher.Matcher<? super Binding<?>> bindingMatcher, com.google.inject.spi.ProvisionListener... listeners) {
      this.binder().bindListener(bindingMatcher, listeners);
   }
}
