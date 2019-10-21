package com.google.inject.spi;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.internal.Errors;

public final class ModuleAnnotatedMethodScannerBinding implements Element {
   private final Object source;
   private final ModuleAnnotatedMethodScanner scanner;

   public ModuleAnnotatedMethodScannerBinding(Object source, ModuleAnnotatedMethodScanner scanner) {
      this.source = Preconditions.checkNotNull(source, "source");
      this.scanner = (ModuleAnnotatedMethodScanner)Preconditions.checkNotNull(scanner, "scanner");
   }

   public Object getSource() {
      return this.source;
   }

   public ModuleAnnotatedMethodScanner getScanner() {
      return this.scanner;
   }

   public <T> T acceptVisitor(ElementVisitor<T> visitor) {
      return visitor.visit(this);
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).scanModulesForAnnotatedMethods(this.scanner);
   }

   public String toString() {
      return this.scanner + " which scans for " + this.scanner.annotationClasses() + " (bound at " + Errors.convert(this.source) + ")";
   }
}
