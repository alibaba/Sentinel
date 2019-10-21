package com.google.inject;

import com.google.inject.internal.CircularDependencyProxy;
import com.google.inject.internal.LinkedBindingImpl;
import com.google.inject.internal.SingletonScope;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.ExposedBinding;
import java.lang.annotation.Annotation;

public class Scopes {
   public static final Scope SINGLETON = new SingletonScope();
   public static final Scope NO_SCOPE = new Scope() {
      public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
         return unscoped;
      }

      public String toString() {
         return "Scopes.NO_SCOPE";
      }
   };
   private static final BindingScopingVisitor<Boolean> IS_SINGLETON_VISITOR = new BindingScopingVisitor<Boolean>() {
      public Boolean visitNoScoping() {
         return false;
      }

      public Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
         return scopeAnnotation == Singleton.class || scopeAnnotation == javax.inject.Singleton.class;
      }

      public Boolean visitScope(Scope scope) {
         return scope == Scopes.SINGLETON;
      }

      public Boolean visitEagerSingleton() {
         return true;
      }
   };

   private Scopes() {
   }

   public static boolean isSingleton(Binding<?> binding) {
      while(true) {
         boolean singleton = (Boolean)binding.acceptScopingVisitor(IS_SINGLETON_VISITOR);
         if (singleton) {
            return true;
         }

         if (binding instanceof LinkedBindingImpl) {
            LinkedBindingImpl<?> linkedBinding = (LinkedBindingImpl)binding;
            Injector injector = linkedBinding.getInjector();
            if (injector != null) {
               binding = injector.getBinding(linkedBinding.getLinkedKey());
               continue;
            }
         } else if (binding instanceof ExposedBinding) {
            ExposedBinding<?> exposedBinding = (ExposedBinding)binding;
            Injector injector = exposedBinding.getPrivateElements().getInjector();
            if (injector != null) {
               binding = injector.getBinding(exposedBinding.getKey());
               continue;
            }
         }

         return false;
      }
   }

   public static boolean isScoped(Binding<?> binding, final Scope scope, final Class<? extends Annotation> scopeAnnotation) {
      while(true) {
         boolean matches = (Boolean)binding.acceptScopingVisitor(new BindingScopingVisitor<Boolean>() {
            public Boolean visitNoScoping() {
               return false;
            }

            public Boolean visitScopeAnnotation(Class<? extends Annotation> visitedAnnotation) {
               return visitedAnnotation == scopeAnnotation;
            }

            public Boolean visitScope(Scope visitedScope) {
               return visitedScope == scope;
            }

            public Boolean visitEagerSingleton() {
               return false;
            }
         });
         if (matches) {
            return true;
         }

         if (binding instanceof LinkedBindingImpl) {
            LinkedBindingImpl<?> linkedBinding = (LinkedBindingImpl)binding;
            Injector injector = linkedBinding.getInjector();
            if (injector != null) {
               binding = injector.getBinding(linkedBinding.getLinkedKey());
               continue;
            }
         } else if (binding instanceof ExposedBinding) {
            ExposedBinding<?> exposedBinding = (ExposedBinding)binding;
            Injector injector = exposedBinding.getPrivateElements().getInjector();
            if (injector != null) {
               binding = injector.getBinding(exposedBinding.getKey());
               continue;
            }
         }

         return false;
      }
   }

   public static boolean isCircularProxy(Object object) {
      return object instanceof CircularDependencyProxy;
   }
}
