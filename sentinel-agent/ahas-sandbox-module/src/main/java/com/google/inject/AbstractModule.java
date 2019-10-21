package com.google.inject;

import com.google.common.base.Preconditions;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeConverter;

import java.lang.annotation.Annotation;

public abstract class AbstractModule implements Module {
   Binder binder;

   public final synchronized void configure(Binder builder) {
      Preconditions.checkState(this.binder == null, "Re-entry is not allowed.");
      this.binder = (Binder)Preconditions.checkNotNull(builder, "builder");

      try {
         this.configure();
      } finally {
         this.binder = null;
      }

   }

   protected void configure() {
   }

   protected Binder binder() {
      Preconditions.checkState(this.binder != null, "The binder can only be used inside configure()");
      return this.binder;
   }

   protected void bindScope(Class<? extends Annotation> scopeAnnotation, Scope scope) {
      this.binder().bindScope(scopeAnnotation, scope);
   }

   protected <T> LinkedBindingBuilder<T> bind(Key<T> key) {
      return this.binder().bind(key);
   }

   protected <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
      return this.binder().bind(typeLiteral);
   }

   protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
      return this.binder().bind(clazz);
   }

   protected AnnotatedConstantBindingBuilder bindConstant() {
      return this.binder().bindConstant();
   }

   protected void install(Module module) {
      this.binder().install(module);
   }

   protected void addError(String message, Object... arguments) {
      this.binder().addError(message, arguments);
   }

   protected void addError(Throwable t) {
      this.binder().addError(t);
   }

   protected void addError(Message message) {
      this.binder().addError(message);
   }

   protected void requestInjection(Object instance) {
      this.binder().requestInjection(instance);
   }

   protected void requestStaticInjection(Class... types) {
      this.binder().requestStaticInjection(types);
   }

   protected void requireBinding(Key<?> key) {
      this.binder().getProvider(key);
   }

   protected void requireBinding(Class<?> type) {
      this.binder().getProvider(type);
   }

   protected <T> Provider<T> getProvider(Key<T> key) {
      return this.binder().getProvider(key);
   }

   protected <T> Provider<T> getProvider(Class<T> type) {
      return this.binder().getProvider(type);
   }

   protected void convertToTypes(com.google.inject.matcher.Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
      this.binder().convertToTypes(typeMatcher, converter);
   }

   protected Stage currentStage() {
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

   protected void bindListener(com.google.inject.matcher.Matcher<? super Binding<?>> bindingMatcher, com.google.inject.spi.ProvisionListener... listener) {
      this.binder().bindListener(bindingMatcher, listener);
   }
}
