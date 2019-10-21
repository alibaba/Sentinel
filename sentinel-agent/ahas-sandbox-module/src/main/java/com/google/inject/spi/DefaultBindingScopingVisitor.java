package com.google.inject.spi;

import com.google.inject.Scope;
import java.lang.annotation.Annotation;

public class DefaultBindingScopingVisitor<V> implements com.google.inject.spi.BindingScopingVisitor<V> {
   protected V visitOther() {
      return null;
   }

   public V visitEagerSingleton() {
      return this.visitOther();
   }

   public V visitScope(Scope scope) {
      return this.visitOther();
   }

   public V visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
      return this.visitOther();
   }

   public V visitNoScoping() {
      return this.visitOther();
   }
}
