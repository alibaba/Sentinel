package com.google.inject.spi;

import com.google.inject.Binder;
import com.google.inject.Binding;

public interface ExposedBinding<T> extends Binding<T>, HasDependencies {
   PrivateElements getPrivateElements();

   void applyTo(Binder var1);
}
