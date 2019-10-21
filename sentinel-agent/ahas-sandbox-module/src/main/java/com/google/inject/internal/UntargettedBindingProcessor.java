package com.google.inject.internal;

import com.google.inject.Binding;
import com.google.inject.spi.UntargettedBinding;

class UntargettedBindingProcessor extends AbstractBindingProcessor {
   UntargettedBindingProcessor(Errors errors, ProcessedBindingData bindingData) {
      super(errors, bindingData);
   }

   public <T> Boolean visit(Binding<T> binding) {
      return (Boolean)binding.acceptTargetVisitor(new Processor<T, Boolean>((BindingImpl)binding) {
         public Boolean visit(UntargettedBinding<? extends T> untargetted) {
            this.prepareBinding();
            if (this.key.getAnnotationType() != null) {
               UntargettedBindingProcessor.this.errors.missingImplementationWithHint(this.key, UntargettedBindingProcessor.this.injector);
               UntargettedBindingProcessor.this.putBinding(UntargettedBindingProcessor.this.invalidBinding(UntargettedBindingProcessor.this.injector, this.key, this.source));
               return true;
            } else {
               try {
                  BindingImpl<T> binding = UntargettedBindingProcessor.this.injector.createUninitializedBinding(this.key, this.scoping, this.source, UntargettedBindingProcessor.this.errors, false);
                  this.scheduleInitialization(binding);
                  UntargettedBindingProcessor.this.putBinding(binding);
               } catch (ErrorsException var3) {
                  UntargettedBindingProcessor.this.errors.merge(var3.getErrors());
                  UntargettedBindingProcessor.this.putBinding(UntargettedBindingProcessor.this.invalidBinding(UntargettedBindingProcessor.this.injector, this.key, this.source));
               }

               return true;
            }
         }

         protected Boolean visitOther(Binding<? extends T> binding) {
            return false;
         }
      });
   }
}
