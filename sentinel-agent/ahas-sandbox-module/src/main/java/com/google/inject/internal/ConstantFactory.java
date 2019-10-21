package com.google.inject.internal;

import com.google.common.base.MoreObjects;
import com.google.inject.spi.Dependency;

final class ConstantFactory<T> implements InternalFactory<T> {
   private final Initializable<T> initializable;

   public ConstantFactory(Initializable<T> initializable) {
      this.initializable = initializable;
   }

   public T get(InternalContext context, Dependency<?> dependency, boolean linked) throws InternalProvisionException {
      return this.initializable.get();
   }

   public String toString() {
      return MoreObjects.toStringHelper(ConstantFactory.class).add("value", this.initializable).toString();
   }
}
