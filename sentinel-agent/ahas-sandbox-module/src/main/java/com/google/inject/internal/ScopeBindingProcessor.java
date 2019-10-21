package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.inject.Scope;
import com.google.inject.spi.ScopeBinding;
import java.lang.annotation.Annotation;

final class ScopeBindingProcessor extends AbstractProcessor {
   ScopeBindingProcessor(Errors errors) {
      super(errors);
   }

   public Boolean visit(ScopeBinding command) {
      Scope scope = (Scope)Preconditions.checkNotNull(command.getScope(), "scope");
      Class<? extends Annotation> annotationType = (Class)Preconditions.checkNotNull(command.getAnnotationType(), "annotation type");
      if (!Annotations.isScopeAnnotation(annotationType)) {
         this.errors.missingScopeAnnotation(annotationType);
      }

      if (!Annotations.isRetainedAtRuntime(annotationType)) {
         this.errors.missingRuntimeRetention(annotationType);
      }

      ScopeBinding existing = this.injector.state.getScopeBinding(annotationType);
      if (existing != null) {
         if (!scope.equals(existing.getScope())) {
            this.errors.duplicateScopes(existing, annotationType, scope);
         }
      } else {
         this.injector.state.putScopeBinding(annotationType, command);
      }

      return true;
   }
}
