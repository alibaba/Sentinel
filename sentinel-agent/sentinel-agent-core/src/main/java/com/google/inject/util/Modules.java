package com.google.inject.util;

import com.google.common.collect.*;
import com.google.inject.*;
import com.google.inject.internal.Errors;
import com.google.inject.spi.*;

import java.lang.annotation.Annotation;
import java.util.*;

public final class Modules {
   public static final Module EMPTY_MODULE = new EmptyModule();

   private Modules() {
   }

   public static OverriddenModuleBuilder override(Module... modules) {
      return new RealOverriddenModuleBuilder(Arrays.asList(modules));
   }

   public static OverriddenModuleBuilder override(Iterable<? extends Module> modules) {
      return new RealOverriddenModuleBuilder(modules);
   }

   public static Module combine(Module... modules) {
      return combine((Iterable)ImmutableSet.copyOf(modules));
   }

   public static Module combine(Iterable<? extends Module> modules) {
      return new CombinedModule(modules);
   }

   private static Module extractScanners(Iterable<Element> elements) {
      final List<ModuleAnnotatedMethodScannerBinding> scanners = Lists.newArrayList();
      ElementVisitor<Void> visitor = new DefaultElementVisitor<Void>() {
         public Void visit(ModuleAnnotatedMethodScannerBinding binding) {
            scanners.add(binding);
            return null;
         }
      };
      Iterator i$ = elements.iterator();

      while(i$.hasNext()) {
         Element element = (Element)i$.next();
         element.acceptVisitor(visitor);
      }

      return new AbstractModule() {
         protected void configure() {
            Iterator i$ = scanners.iterator();

            while(i$.hasNext()) {
               ModuleAnnotatedMethodScannerBinding scanner = (ModuleAnnotatedMethodScannerBinding)i$.next();
               scanner.applyTo(this.binder());
            }

         }
      };
   }

   private static class ModuleWriter extends DefaultElementVisitor<Void> {
      protected final Binder binder;

      ModuleWriter(Binder binder) {
         this.binder = binder.skipSources(this.getClass());
      }

      protected Void visitOther(Element element) {
         element.applyTo(this.binder);
         return null;
      }

      void writeAll(Iterable<? extends Element> elements) {
         Iterator i$ = elements.iterator();

         while(i$.hasNext()) {
            Element element = (Element)i$.next();
            element.acceptVisitor(this);
         }

      }
   }

   static class OverrideModule extends AbstractModule {
      private final ImmutableSet<Module> overrides;
      private final ImmutableSet<Module> baseModules;

      OverrideModule(Iterable<? extends Module> overrides, ImmutableSet<Module> baseModules) {
         this.overrides = ImmutableSet.copyOf(overrides);
         this.baseModules = baseModules;
      }

      public void configure() {
         Binder baseBinder = this.binder();
         List<Element> baseElements = Elements.getElements(this.currentStage(), (Iterable)this.baseModules);
         if (baseElements.size() == 1) {
            Element element = (Element)Iterables.getOnlyElement(baseElements);
            if (element instanceof PrivateElements) {
               PrivateElements privateElements = (PrivateElements)element;
               PrivateBinder privateBinder = ((Binder)baseBinder).newPrivateBinder().withSource(privateElements.getSource());
               Iterator i$ = privateElements.getExposedKeys().iterator();

               while(i$.hasNext()) {
                  Key exposed = (Key)i$.next();
                  privateBinder.withSource(privateElements.getExposedSource(exposed)).expose(exposed);
               }

               baseBinder = privateBinder;
               baseElements = privateElements.getElements();
            }
         }

         Binder binder = ((Binder)baseBinder).skipSources(this.getClass());
         LinkedHashSet<Element> elements = new LinkedHashSet(baseElements);
         Module scannersModule = Modules.extractScanners(elements);
         List<Element> overrideElements = Elements.getElements(this.currentStage(), (Iterable)ImmutableList.builder().addAll(this.overrides).add(scannersModule).build());
         final Set<Key<?>> overriddenKeys = Sets.newHashSet();
         final Map<Class<? extends Annotation>, ScopeBinding> overridesScopeAnnotations = Maps.newHashMap();
         (new ModuleWriter(binder) {
            public <T> Void visit(Binding<T> binding) {
               overriddenKeys.add(binding.getKey());
               return (Void)super.visit(binding);
            }

            public Void visit(ScopeBinding scopeBinding) {
               overridesScopeAnnotations.put(scopeBinding.getAnnotationType(), scopeBinding);
               return (Void)super.visit(scopeBinding);
            }

            public Void visit(PrivateElements privateElements) {
               overriddenKeys.addAll(privateElements.getExposedKeys());
               return (Void)super.visit(privateElements);
            }
         }).writeAll(overrideElements);
         final Map<Scope, List<Object>> scopeInstancesInUse = Maps.newHashMap();
         final List<ScopeBinding> scopeBindings = Lists.newArrayList();
         (new ModuleWriter(binder) {
            public <T> Void visit(Binding<T> binding) {
               if (!overriddenKeys.remove(binding.getKey())) {
                  super.visit(binding);
                  Scope scope = OverrideModule.this.getScopeInstanceOrNull(binding);
                  if (scope != null) {
                     List<Object> existing = (List)scopeInstancesInUse.get(scope);
                     if (existing == null) {
                        existing = Lists.newArrayList();
                        scopeInstancesInUse.put(scope, existing);
                     }

                     ((List)existing).add(binding.getSource());
                  }
               }

               return null;
            }

            void rewrite(Binder binder, PrivateElements privateElements, Set<Key<?>> keysToSkip) {
               PrivateBinder privateBinder = binder.withSource(privateElements.getSource()).newPrivateBinder();
               Set<Key<?>> skippedExposes = Sets.newHashSet();
               Iterator i$ = privateElements.getExposedKeys().iterator();

               while(i$.hasNext()) {
                  Key<?> key = (Key)i$.next();
                  if (keysToSkip.remove(key)) {
                     skippedExposes.add(key);
                  } else {
                     privateBinder.withSource(privateElements.getExposedSource(key)).expose(key);
                  }
               }

               i$ = privateElements.getElements().iterator();

               while(true) {
                  Element element;
                  do {
                     if (!i$.hasNext()) {
                        return;
                     }

                     element = (Element)i$.next();
                  } while(element instanceof Binding && skippedExposes.remove(((Binding)element).getKey()));

                  if (element instanceof PrivateElements) {
                     this.rewrite(privateBinder, (PrivateElements)element, skippedExposes);
                  } else {
                     element.applyTo(privateBinder);
                  }
               }
            }

            public Void visit(PrivateElements privateElements) {
               this.rewrite(this.binder, privateElements, overriddenKeys);
               return null;
            }

            public Void visit(ScopeBinding scopeBinding) {
               scopeBindings.add(scopeBinding);
               return null;
            }
         }).writeAll(elements);
         (new ModuleWriter(binder) {
            public Void visit(ScopeBinding scopeBinding) {
               ScopeBinding overideBinding = (ScopeBinding)overridesScopeAnnotations.remove(scopeBinding.getAnnotationType());
               if (overideBinding == null) {
                  super.visit(scopeBinding);
               } else {
                  List<Object> usedSources = (List)scopeInstancesInUse.get(scopeBinding.getScope());
                  if (usedSources != null) {
                     StringBuilder sb = new StringBuilder("The scope for @%s is bound directly and cannot be overridden.");
                     sb.append("%n     original binding at " + Errors.convert(scopeBinding.getSource()));
                     Iterator i$ = usedSources.iterator();

                     while(i$.hasNext()) {
                        Object usedSource = i$.next();
                        sb.append("%n     bound directly at " + Errors.convert(usedSource) + "");
                     }

                     this.binder.withSource(overideBinding.getSource()).addError(sb.toString(), scopeBinding.getAnnotationType().getSimpleName());
                  }
               }

               return null;
            }
         }).writeAll(scopeBindings);
      }

      private Scope getScopeInstanceOrNull(Binding<?> binding) {
         return (Scope)binding.acceptScopingVisitor(new DefaultBindingScopingVisitor<Scope>() {
            public Scope visitScope(Scope scope) {
               return scope;
            }
         });
      }
   }

   private static final class RealOverriddenModuleBuilder implements OverriddenModuleBuilder {
      private final ImmutableSet<Module> baseModules;

      private RealOverriddenModuleBuilder(Iterable<? extends Module> baseModules) {
         this.baseModules = ImmutableSet.copyOf(baseModules);
      }

      public Module with(Module... overrides) {
         return this.with((Iterable)Arrays.asList(overrides));
      }

      public Module with(Iterable<? extends Module> overrides) {
         return new OverrideModule(overrides, this.baseModules);
      }

      // $FF: synthetic method
      RealOverriddenModuleBuilder(Iterable x0, Object x1) {
         this(x0);
      }
   }

   public interface OverriddenModuleBuilder {
      Module with(Module... var1);

      Module with(Iterable<? extends Module> var1);
   }

   private static class CombinedModule implements Module {
      final Set<Module> modulesSet;

      CombinedModule(Iterable<? extends Module> modules) {
         this.modulesSet = ImmutableSet.copyOf(modules);
      }

      public void configure(Binder binder) {
         binder = binder.skipSources(this.getClass());
         Iterator i$ = this.modulesSet.iterator();

         while(i$.hasNext()) {
            Module module = (Module)i$.next();
            binder.install(module);
         }

      }
   }

   private static class EmptyModule implements Module {
      private EmptyModule() {
      }

      public void configure(Binder binder) {
      }

      // $FF: synthetic method
      EmptyModule(Object x0) {
         this();
      }
   }
}
