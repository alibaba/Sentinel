package com.google.inject.internal;

import com.google.inject.spi.DefaultElementVisitor;

import java.util.Iterator;
import java.util.List;

abstract class AbstractProcessor extends DefaultElementVisitor<Boolean> {
   protected Errors errors;
   protected InjectorImpl injector;

   protected AbstractProcessor(Errors errors) {
      this.errors = errors;
   }

   public void process(Iterable<InjectorShell> isolatedInjectorBuilders) {
      Iterator i$ = isolatedInjectorBuilders.iterator();

      while(i$.hasNext()) {
         InjectorShell injectorShell = (InjectorShell)i$.next();
         this.process(injectorShell.getInjector(), injectorShell.getElements());
      }

   }

   public void process(InjectorImpl injector, List<com.google.inject.spi.Element> elements) {
      Errors errorsAnyElement = this.errors;
      this.injector = injector;

      try {
         Iterator i = elements.iterator();

         while(i.hasNext()) {
            com.google.inject.spi.Element element = (com.google.inject.spi.Element)i.next();
            this.errors = errorsAnyElement.withSource(element.getSource());
            Boolean allDone = (Boolean)element.acceptVisitor(this);
            if (allDone) {
               i.remove();
            }
         }
      } finally {
         this.errors = errorsAnyElement;
         this.injector = null;
      }

   }

   protected Boolean visitOther(com.google.inject.spi.Element element) {
      return false;
   }
}
