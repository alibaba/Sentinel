package com.google.inject.internal;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

class ProcessedBindingData {
   private final List<CreationListener> creationListeners = Lists.newArrayList();
   private final List<Runnable> uninitializedBindings = Lists.newArrayList();
   private final List<Runnable> delayedUninitializedBindings = Lists.newArrayList();

   void addCreationListener(CreationListener listener) {
      this.creationListeners.add(listener);
   }

   void addUninitializedBinding(Runnable runnable) {
      this.uninitializedBindings.add(runnable);
   }

   void addDelayedUninitializedBinding(Runnable runnable) {
      this.delayedUninitializedBindings.add(runnable);
   }

   void initializeBindings() {
      Iterator i$ = this.uninitializedBindings.iterator();

      while(i$.hasNext()) {
         Runnable initializer = (Runnable)i$.next();
         initializer.run();
      }

   }

   void runCreationListeners(Errors errors) {
      Iterator i$ = this.creationListeners.iterator();

      while(i$.hasNext()) {
         CreationListener creationListener = (CreationListener)i$.next();
         creationListener.notify(errors);
      }

   }

   void initializeDelayedBindings() {
      Iterator i$ = this.delayedUninitializedBindings.iterator();

      while(i$.hasNext()) {
         Runnable initializer = (Runnable)i$.next();
         initializer.run();
      }

   }
}
