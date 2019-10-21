package com.google.inject.spi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.internal.util.StackTraceElements;

import java.util.List;

final class ModuleSource {
   private final String moduleClassName;
   private final ModuleSource parent;
   private final StackTraceElements.InMemoryStackTraceElement[] partialCallStack;

   ModuleSource(Object module, StackTraceElement[] partialCallStack) {
      this((ModuleSource)null, module, partialCallStack);
   }

   private ModuleSource(ModuleSource parent, Object module, StackTraceElement[] partialCallStack) {
      Preconditions.checkNotNull(module, "module cannot be null.");
      Preconditions.checkNotNull(partialCallStack, "partialCallStack cannot be null.");
      this.parent = parent;
      this.moduleClassName = module.getClass().getName();
      this.partialCallStack = StackTraceElements.convertToInMemoryStackTraceElement(partialCallStack);
   }

   String getModuleClassName() {
      return this.moduleClassName;
   }

   StackTraceElement[] getPartialCallStack() {
      return StackTraceElements.convertToStackTraceElement(this.partialCallStack);
   }

   int getPartialCallStackSize() {
      return this.partialCallStack.length;
   }

   ModuleSource createChild(Object module, StackTraceElement[] partialCallStack) {
      return new ModuleSource(this, module, partialCallStack);
   }

   ModuleSource getParent() {
      return this.parent;
   }

   List<String> getModuleClassNames() {
      Builder<String> classNames = ImmutableList.builder();

      for(ModuleSource current = this; current != null; current = current.parent) {
         String className = current.moduleClassName;
         classNames.add(className);
      }

      return classNames.build();
   }

   int size() {
      return this.parent == null ? 1 : this.parent.size() + 1;
   }

   int getStackTraceSize() {
      return this.parent == null ? this.partialCallStack.length : this.parent.getStackTraceSize() + this.partialCallStack.length;
   }

   StackTraceElement[] getStackTrace() {
      int stackTraceSize = this.getStackTraceSize();
      StackTraceElement[] callStack = new StackTraceElement[stackTraceSize];
      int cursor = 0;

      int chunkSize;
      for(ModuleSource current = this; current != null; cursor += chunkSize) {
         StackTraceElement[] chunk = StackTraceElements.convertToStackTraceElement(current.partialCallStack);
         chunkSize = chunk.length;
         System.arraycopy(chunk, 0, callStack, cursor, chunkSize);
         current = current.parent;
      }

      return callStack;
   }
}
