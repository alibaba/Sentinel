package com.google.inject.spi;

import com.google.inject.Binding;

public interface ElementVisitor<V> {
   <T> V visit(Binding<T> var1);

   V visit(ScopeBinding var1);

   V visit(TypeConverterBinding var1);

   V visit(InjectionRequest<?> var1);

   V visit(StaticInjectionRequest var1);

   <T> V visit(ProviderLookup<T> var1);

   <T> V visit(MembersInjectorLookup<T> var1);

   V visit(Message var1);

   V visit(PrivateElements var1);

   V visit(TypeListenerBinding var1);

   V visit(ProvisionListenerBinding var1);

   V visit(RequireExplicitBindingsOption var1);

   V visit(DisableCircularProxiesOption var1);

   V visit(RequireAtInjectOnConstructorsOption var1);

   V visit(RequireExactBindingAnnotationsOption var1);

   V visit(ModuleAnnotatedMethodScannerBinding var1);
}
