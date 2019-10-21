package com.google.inject.spi;

import com.google.inject.internal.util.StackTraceElements;
import java.lang.reflect.Member;

/** @deprecated */
@Deprecated
public final class DependencyAndSource {
   private final Dependency<?> dependency;
   private final Object source;

   public DependencyAndSource(Dependency<?> dependency, Object source) {
      this.dependency = dependency;
      this.source = source;
   }

   public Dependency<?> getDependency() {
      return this.dependency;
   }

   public String getBindingSource() {
      if (this.source instanceof Class) {
         return StackTraceElements.forType((Class)this.source).toString();
      } else {
         return this.source instanceof Member ? StackTraceElements.forMember((Member)this.source).toString() : this.source.toString();
      }
   }

   public String toString() {
      Dependency<?> dep = this.getDependency();
      Object source = this.getBindingSource();
      return dep != null ? "Dependency: " + dep + ", source: " + source : "Source: " + source;
   }
}
