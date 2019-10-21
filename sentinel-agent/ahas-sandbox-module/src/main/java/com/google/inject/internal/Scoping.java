package com.google.inject.internal;

import com.google.common.base.Objects;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.ScopeBinding;
import java.lang.annotation.Annotation;

public abstract class Scoping {
   public static final Scoping UNSCOPED = new Scoping() {
      public <V> V acceptVisitor(BindingScopingVisitor<V> visitor) {
         return visitor.visitNoScoping();
      }

      public Scope getScopeInstance() {
         return Scopes.NO_SCOPE;
      }

      public String toString() {
         return Scopes.NO_SCOPE.toString();
      }

      public void applyTo(ScopedBindingBuilder scopedBindingBuilder) {
      }
   };
   private static final Scoping EXPLICITLY_UNSCOPED = new Scoping() {
      public <V> V acceptVisitor(BindingScopingVisitor<V> visitor) {
         return visitor.visitNoScoping();
      }

      public Scope getScopeInstance() {
         return Scopes.NO_SCOPE;
      }

      public String toString() {
         return Scopes.NO_SCOPE.toString();
      }

      public void applyTo(ScopedBindingBuilder scopedBindingBuilder) {
         scopedBindingBuilder.in(Scopes.NO_SCOPE);
      }
   };
   public static final Scoping SINGLETON_ANNOTATION = new Scoping() {
      public <V> V acceptVisitor(BindingScopingVisitor<V> visitor) {
         return visitor.visitScopeAnnotation(Singleton.class);
      }

      public Class<? extends Annotation> getScopeAnnotation() {
         return Singleton.class;
      }

      public String toString() {
         return Singleton.class.getName();
      }

      public void applyTo(ScopedBindingBuilder scopedBindingBuilder) {
         scopedBindingBuilder.in(Singleton.class);
      }
   };
   public static final Scoping SINGLETON_INSTANCE = new Scoping() {
      public <V> V acceptVisitor(BindingScopingVisitor<V> visitor) {
         return visitor.visitScope(Scopes.SINGLETON);
      }

      public Scope getScopeInstance() {
         return Scopes.SINGLETON;
      }

      public String toString() {
         return Scopes.SINGLETON.toString();
      }

      public void applyTo(ScopedBindingBuilder scopedBindingBuilder) {
         scopedBindingBuilder.in(Scopes.SINGLETON);
      }
   };
   public static final Scoping EAGER_SINGLETON = new Scoping() {
      public <V> V acceptVisitor(BindingScopingVisitor<V> visitor) {
         return visitor.visitEagerSingleton();
      }

      public Scope getScopeInstance() {
         return Scopes.SINGLETON;
      }

      public String toString() {
         return "eager singleton";
      }

      public void applyTo(ScopedBindingBuilder scopedBindingBuilder) {
         scopedBindingBuilder.asEagerSingleton();
      }
   };

   public static Scoping forAnnotation(final Class<? extends Annotation> scopingAnnotation) {
      return scopingAnnotation != Singleton.class && scopingAnnotation != javax.inject.Singleton.class ? new Scoping() {
         public <V> V acceptVisitor(BindingScopingVisitor<V> visitor) {
            return visitor.visitScopeAnnotation(scopingAnnotation);
         }

         public Class<? extends Annotation> getScopeAnnotation() {
            return scopingAnnotation;
         }

         public String toString() {
            return scopingAnnotation.getName();
         }

         public void applyTo(ScopedBindingBuilder scopedBindingBuilder) {
            scopedBindingBuilder.in(scopingAnnotation);
         }
      } : SINGLETON_ANNOTATION;
   }

   public static Scoping forInstance(final Scope scope) {
      if (scope == Scopes.SINGLETON) {
         return SINGLETON_INSTANCE;
      } else {
         return scope == Scopes.NO_SCOPE ? EXPLICITLY_UNSCOPED : new Scoping() {
            public <V> V acceptVisitor(BindingScopingVisitor<V> visitor) {
               return visitor.visitScope(scope);
            }

            public Scope getScopeInstance() {
               return scope;
            }

            public String toString() {
               return scope.toString();
            }

            public void applyTo(ScopedBindingBuilder scopedBindingBuilder) {
               scopedBindingBuilder.in(scope);
            }
         };
      }
   }

   public boolean isExplicitlyScoped() {
      return this != UNSCOPED;
   }

   public boolean isNoScope() {
      return this.getScopeInstance() == Scopes.NO_SCOPE;
   }

   public boolean isEagerSingleton(Stage stage) {
      if (this == EAGER_SINGLETON) {
         return true;
      } else if (stage != Stage.PRODUCTION) {
         return false;
      } else {
         return this == SINGLETON_ANNOTATION || this == SINGLETON_INSTANCE;
      }
   }

   public Scope getScopeInstance() {
      return null;
   }

   public Class<? extends Annotation> getScopeAnnotation() {
      return null;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Scoping)) {
         return false;
      } else {
         Scoping o = (Scoping)obj;
         return Objects.equal(this.getScopeAnnotation(), o.getScopeAnnotation()) && Objects.equal(this.getScopeInstance(), o.getScopeInstance());
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.getScopeAnnotation(), this.getScopeInstance()});
   }

   public abstract <V> V acceptVisitor(BindingScopingVisitor<V> var1);

   public abstract void applyTo(ScopedBindingBuilder var1);

   private Scoping() {
   }

   static <T> InternalFactory<? extends T> scope(Key<T> key, InjectorImpl injector, InternalFactory<? extends T> creator, Object source, Scoping scoping) {
      if (scoping.isNoScope()) {
         return creator;
      } else {
         Scope scope = scoping.getScopeInstance();
         Provider<T> scoped = scope.scope(key, new ProviderToInternalFactoryAdapter(injector, creator));
         return new InternalFactoryToProviderAdapter(scoped, source);
      }
   }

   static Scoping makeInjectable(Scoping scoping, InjectorImpl injector, Errors errors) {
      Class<? extends Annotation> scopeAnnotation = scoping.getScopeAnnotation();
      if (scopeAnnotation == null) {
         return scoping;
      } else {
         ScopeBinding scope = injector.state.getScopeBinding(scopeAnnotation);
         if (scope != null) {
            return forInstance(scope.getScope());
         } else {
            errors.scopeNotFound(scopeAnnotation);
            return UNSCOPED;
         }
      }
   }

   // $FF: synthetic method
   Scoping(Object x0) {
      this();
   }
}
