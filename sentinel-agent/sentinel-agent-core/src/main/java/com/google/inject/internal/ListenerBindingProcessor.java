package com.google.inject.internal;

import com.google.inject.spi.ProvisionListenerBinding;
import com.google.inject.spi.TypeListenerBinding;

final class ListenerBindingProcessor extends AbstractProcessor {
   ListenerBindingProcessor(Errors errors) {
      super(errors);
   }

   public Boolean visit(TypeListenerBinding binding) {
      this.injector.state.addTypeListener(binding);
      return true;
   }

   public Boolean visit(ProvisionListenerBinding binding) {
      this.injector.state.addProvisionListener(binding);
      return true;
   }
}
