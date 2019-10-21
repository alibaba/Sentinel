package com.google.inject.multibindings;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ModuleAnnotatedMethodScanner;
import com.google.inject.util.Modules;
import java.lang.annotation.Annotation;
import java.util.Set;

/** @deprecated */
@Deprecated
public class MultibindingsScanner {
   private MultibindingsScanner() {
   }

   /** @deprecated */
   @Deprecated
   public static Module asModule() {
      return Modules.EMPTY_MODULE;
   }

   /** @deprecated */
   @Deprecated
   public static ModuleAnnotatedMethodScanner scanner() {
      return new ModuleAnnotatedMethodScanner() {
         public Set<? extends Class<? extends Annotation>> annotationClasses() {
            return ImmutableSet.of();
         }

         public <T> Key<T> prepareMethod(Binder binder, Annotation annotation, Key<T> key, InjectionPoint injectionPoint) {
            throw new IllegalStateException("Unexpected annotation: " + annotation);
         }
      };
   }
}
