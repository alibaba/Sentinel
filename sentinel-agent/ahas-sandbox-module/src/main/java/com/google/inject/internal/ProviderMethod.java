package com.google.inject.internal;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Exposed;
import com.google.inject.Key;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.spi.ProvidesMethodBinding;
import com.google.inject.spi.ProvidesMethodTargetVisitor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public abstract class ProviderMethod<T> extends InternalProviderInstanceBindingImpl.CyclicFactory<T> implements HasDependencies, ProvidesMethodBinding<T>, ProviderWithExtensionVisitor<T> {
   protected final Object instance;
   protected final Method method;
   private final Key<T> key;
   private final Class<? extends Annotation> scopeAnnotation;
   private final ImmutableSet<Dependency<?>> dependencies;
   private final boolean exposed;
   private final Annotation annotation;
   private SingleParameterInjector<?>[] parameterInjectors;

   static <T> ProviderMethod<T> create(Key<T> key, Method method, Object instance, ImmutableSet<Dependency<?>> dependencies, Class<? extends Annotation> scopeAnnotation, boolean skipFastClassGeneration, Annotation annotation) {
      int modifiers = method.getModifiers();
      if (!Modifier.isPublic(modifiers) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
         method.setAccessible(true);
      }

      return new ReflectionProviderMethod(key, method, instance, dependencies, scopeAnnotation, annotation);
   }

   private ProviderMethod(Key<T> key, Method method, Object instance, ImmutableSet<Dependency<?>> dependencies, Class<? extends Annotation> scopeAnnotation, Annotation annotation) {
      super(InternalProviderInstanceBindingImpl.InitializationTiming.EAGER);
      this.key = key;
      this.scopeAnnotation = scopeAnnotation;
      this.instance = instance;
      this.dependencies = dependencies;
      this.method = method;
      this.exposed = method.isAnnotationPresent(Exposed.class);
      this.annotation = annotation;
   }

   public Key<T> getKey() {
      return this.key;
   }

   public Method getMethod() {
      return this.method;
   }

   public Object getInstance() {
      return this.instance;
   }

   public Object getEnclosingInstance() {
      return this.instance;
   }

   public Annotation getAnnotation() {
      return this.annotation;
   }

   public void configure(Binder binder) {
      binder = binder.withSource(this.method);
      if (this.scopeAnnotation != null) {
         binder.bind(this.key).toProvider((Provider)this).in(this.scopeAnnotation);
      } else {
         binder.bind(this.key).toProvider((Provider)this);
      }

      if (this.exposed) {
         ((PrivateBinder)binder).expose(this.key);
      }

   }

   void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
      this.parameterInjectors = injector.getParametersInjectors(this.dependencies.asList(), errors);
   }

   protected T doProvision(InternalContext context, Dependency<?> dependency) throws InternalProvisionException {
      try {
         T t = this.doProvision(SingleParameterInjector.getAll(context, this.parameterInjectors));
         if (t == null && !dependency.isNullable()) {
            InternalProvisionException.onNullInjectedIntoNonNullableDependency(this.getMethod(), dependency);
         }

         return t;
      } catch (IllegalAccessException var5) {
         throw new AssertionError(var5);
      } catch (InvocationTargetException var6) {
         Throwable cause = var6.getCause() != null ? var6.getCause() : var6;
         throw InternalProvisionException.errorInProvider((Throwable)cause).addSource(this.getSource());
      }
   }

   abstract T doProvision(Object[] var1) throws IllegalAccessException, InvocationTargetException;

   public Set<Dependency<?>> getDependencies() {
      return this.dependencies;
   }

   public <B, V> V acceptExtensionVisitor(BindingTargetVisitor<B, V> visitor, ProviderInstanceBinding<? extends B> binding) {
      if (visitor instanceof ProvidesMethodTargetVisitor) {
         return ((ProvidesMethodTargetVisitor<T, V>) visitor).visit(this);
      }
      return visitor.visit(binding);
   }

   public String toString() {
      String annotationString = this.annotation.toString();
      if (this.annotation.annotationType() == Provides.class) {
         annotationString = "@Provides";
      } else if (annotationString.endsWith("()")) {
         annotationString = annotationString.substring(0, annotationString.length() - 2);
      }

      return annotationString + " " + StackTraceElements.forMember(this.method);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof ProviderMethod)) {
         return false;
      } else {
         ProviderMethod<?> o = (ProviderMethod)obj;
         return this.method.equals(o.method) && this.instance.equals(o.instance) && this.annotation.equals(o.annotation);
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.method, this.annotation});
   }

   // $FF: synthetic method
   ProviderMethod(Key x0, Method x1, Object x2, ImmutableSet x3, Class x4, Annotation x5, Object x6) {
      this(x0, x1, x2, x3, x4, x5);
   }

   private static final class ReflectionProviderMethod<T> extends ProviderMethod<T> {
      ReflectionProviderMethod(
              Key<T> key,
              Method method,
              Object instance,
              ImmutableSet<Dependency<?>> dependencies,
              Class<? extends Annotation> scopeAnnotation,
              Annotation annotation) {
         super(key, method, instance, dependencies, scopeAnnotation, annotation);
      }

      @SuppressWarnings("unchecked")
      @Override
      T doProvision(Object[] parameters) throws IllegalAccessException, InvocationTargetException {
         return (T) method.invoke(instance, parameters);
      }
   }
}
