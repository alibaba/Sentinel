package com.google.inject.internal;

import com.google.inject.Key;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.PrivateElements;

final class ExposedKeyFactory<T> implements InternalFactory<T>, CreationListener {
   private final Key<T> key;
   private final PrivateElements privateElements;
   private BindingImpl<T> delegate;

   ExposedKeyFactory(Key<T> key, PrivateElements privateElements) {
      this.key = key;
      this.privateElements = privateElements;
   }

   public void notify(Errors errors) {
      InjectorImpl privateInjector = (InjectorImpl)this.privateElements.getInjector();
      BindingImpl<T> explicitBinding = privateInjector.state.getExplicitBinding(this.key);
      if (explicitBinding.getInternalFactory() == this) {
         errors.withSource(explicitBinding.getSource()).exposedButNotBound(this.key);
      } else {
         this.delegate = explicitBinding;
      }
   }

   public T get(InternalContext context, Dependency<?> dependency, boolean linked) throws InternalProvisionException {
      return this.delegate.getInternalFactory().get(context, dependency, linked);
   }
}
