package com.google.inject.spi;

import com.google.inject.Binding;

public abstract class DefaultElementVisitor<V> implements ElementVisitor<V> {
   protected V visitOther(Element element) {
      return null;
   }

   public V visit(Message message) {
      return this.visitOther(message);
   }

   public <T> V visit(Binding<T> binding) {
      return this.visitOther(binding);
   }

   public V visit(ScopeBinding scopeBinding) {
      return this.visitOther(scopeBinding);
   }

   public V visit(TypeConverterBinding typeConverterBinding) {
      return this.visitOther(typeConverterBinding);
   }

   public <T> V visit(ProviderLookup<T> providerLookup) {
      return this.visitOther(providerLookup);
   }

   public V visit(InjectionRequest<?> injectionRequest) {
      return this.visitOther(injectionRequest);
   }

   public V visit(StaticInjectionRequest staticInjectionRequest) {
      return this.visitOther(staticInjectionRequest);
   }

   public V visit(PrivateElements privateElements) {
      return this.visitOther(privateElements);
   }

   public <T> V visit(MembersInjectorLookup<T> lookup) {
      return this.visitOther(lookup);
   }

   public V visit(TypeListenerBinding binding) {
      return this.visitOther(binding);
   }

   public V visit(ProvisionListenerBinding binding) {
      return this.visitOther(binding);
   }

   public V visit(DisableCircularProxiesOption option) {
      return this.visitOther(option);
   }

   public V visit(RequireExplicitBindingsOption option) {
      return this.visitOther(option);
   }

   public V visit(RequireAtInjectOnConstructorsOption option) {
      return this.visitOther(option);
   }

   public V visit(RequireExactBindingAnnotationsOption option) {
      return this.visitOther(option);
   }

   public V visit(ModuleAnnotatedMethodScannerBinding binding) {
      return this.visitOther(binding);
   }
}
