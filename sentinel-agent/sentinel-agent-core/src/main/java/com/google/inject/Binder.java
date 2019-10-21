package com.google.inject;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.*;

import java.lang.annotation.Annotation;

public interface Binder {
   void bindScope(Class<? extends Annotation> var1, Scope var2);

   <T> LinkedBindingBuilder<T> bind(Key<T> var1);

   <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> var1);

   <T> AnnotatedBindingBuilder<T> bind(Class<T> var1);

   AnnotatedConstantBindingBuilder bindConstant();

   <T> void requestInjection(TypeLiteral<T> var1, T var2);

   void requestInjection(Object var1);

   void requestStaticInjection(Class... var1);

   void install(Module var1);

   Stage currentStage();

   void addError(String var1, Object... var2);

   void addError(Throwable var1);

   void addError(Message var1);

   <T> Provider<T> getProvider(Key<T> var1);

   <T> Provider<T> getProvider(Dependency<T> var1);

   <T> Provider<T> getProvider(Class<T> var1);

   <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> var1);

   <T> MembersInjector<T> getMembersInjector(Class<T> var1);

   void convertToTypes(Matcher<? super TypeLiteral<?>> var1, TypeConverter var2);

   void bindListener(Matcher<? super TypeLiteral<?>> var1, TypeListener var2);

   void bindListener(Matcher<? super Binding<?>> var1, ProvisionListener... var2);

   Binder withSource(Object var1);

   Binder skipSources(Class... var1);

   PrivateBinder newPrivateBinder();

   void requireExplicitBindings();

   void disableCircularProxies();

   void requireAtInjectOnConstructors();

   void requireExactBindingAnnotations();

   void scanModulesForAnnotatedMethods(ModuleAnnotatedMethodScanner var1);
}
