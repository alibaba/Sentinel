package com.google.inject;

import com.google.inject.internal.InternalInjectorCreator;
import java.util.Arrays;

public final class Guice {
   private Guice() {
   }

   public static Injector createInjector(Module... modules) {
      return createInjector((Iterable)Arrays.asList(modules));
   }

   public static Injector createInjector(Iterable<? extends Module> modules) {
      return createInjector(Stage.DEVELOPMENT, modules);
   }

   public static Injector createInjector(Stage stage, Module... modules) {
      return createInjector(stage, (Iterable)Arrays.asList(modules));
   }

   public static Injector createInjector(Stage stage, Iterable<? extends Module> modules) {
      return (new InternalInjectorCreator()).stage(stage).addModules(modules).build();
   }
}
