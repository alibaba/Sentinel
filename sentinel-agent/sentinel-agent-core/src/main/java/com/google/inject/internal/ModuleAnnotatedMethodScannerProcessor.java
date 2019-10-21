package com.google.inject.internal;

import com.google.inject.spi.ModuleAnnotatedMethodScannerBinding;

final class ModuleAnnotatedMethodScannerProcessor extends AbstractProcessor {
   ModuleAnnotatedMethodScannerProcessor(Errors errors) {
      super(errors);
   }

   public Boolean visit(ModuleAnnotatedMethodScannerBinding command) {
      this.injector.state.addScanner(command);
      return true;
   }
}
