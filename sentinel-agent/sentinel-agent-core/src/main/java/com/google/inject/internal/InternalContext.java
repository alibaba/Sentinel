package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Key;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.DependencyAndSource;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class InternalContext implements AutoCloseable {
   private final InjectorImpl.InjectorOptions options;
   private final Map<Object, ConstructionContext<?>> constructionContexts = new IdentityHashMap();
   private Dependency<?> dependency;
   private Object[] dependencyStack = new Object[16];
   private int dependencyStackSize = 0;
   private int enterCount;
   private final Object[] toClear;

   InternalContext(InjectorImpl.InjectorOptions options, Object[] toClear) {
      this.options = options;
      this.toClear = toClear;
      this.enterCount = 1;
   }

   void enter() {
      ++this.enterCount;
   }

   public void close() {
      int newCount = --this.enterCount;
      if (newCount < 0) {
         throw new IllegalStateException("Called close() too many times");
      } else {
         if (newCount == 0) {
            this.toClear[0] = null;
         }

      }
   }

   InjectorImpl.InjectorOptions getInjectorOptions() {
      return this.options;
   }

   <T> ConstructionContext<T> getConstructionContext(Object key) {
      ConstructionContext<T> constructionContext = (ConstructionContext)this.constructionContexts.get(key);
      if (constructionContext == null) {
         constructionContext = new ConstructionContext();
         this.constructionContexts.put(key, constructionContext);
      }

      return constructionContext;
   }

   Dependency<?> getDependency() {
      return this.dependency;
   }

   Dependency<?> pushDependency(Dependency<?> dependency, Object source) {
      Dependency<?> previous = this.dependency;
      this.dependency = dependency;
      this.doPushState(dependency, source);
      return previous;
   }

   void popStateAndSetDependency(Dependency<?> newDependency) {
      this.popState();
      this.dependency = newDependency;
   }

   void pushState(Key<?> key, Object source) {
      this.doPushState(key, source);
   }

   private void doPushState(Object dependencyOrKey, Object source) {
      int localSize = this.dependencyStackSize;
      Object[] localStack = this.dependencyStack;
      if (localStack.length < localSize + 2) {
         localStack = this.dependencyStack = Arrays.copyOf(localStack, localStack.length * 3 / 2 + 2);
      }

      localStack[localSize++] = dependencyOrKey;
      localStack[localSize++] = source;
      this.dependencyStackSize = localSize;
   }

   void popState() {
      this.dependencyStackSize -= 2;
   }

   List<DependencyAndSource> getDependencyChain() {
      Builder<DependencyAndSource> builder = ImmutableList.builder();

      for(int i = 0; i < this.dependencyStackSize; i += 2) {
         Object evenEntry = this.dependencyStack[i];
         Dependency dependency;
         if (evenEntry instanceof Key) {
            dependency = Dependency.get((Key)evenEntry);
         } else {
            dependency = (Dependency)evenEntry;
         }

         builder.add(new DependencyAndSource(dependency, this.dependencyStack[i + 1]));
      }

      return builder.build();
   }
}
