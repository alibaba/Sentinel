package com.google.inject.internal;

import com.google.common.collect.Lists;
import com.google.inject.spi.PrivateElements;
import java.util.List;

final class PrivateElementProcessor extends AbstractProcessor {
   private final List<InjectorShell.Builder> injectorShellBuilders = Lists.newArrayList();

   PrivateElementProcessor(Errors errors) {
      super(errors);
   }

   public Boolean visit(PrivateElements privateElements) {
      InjectorShell.Builder builder = (new InjectorShell.Builder()).parent(this.injector).privateElements(privateElements);
      this.injectorShellBuilders.add(builder);
      return true;
   }

   public List<InjectorShell.Builder> getInjectorShellBuilders() {
      return this.injectorShellBuilders;
   }
}
