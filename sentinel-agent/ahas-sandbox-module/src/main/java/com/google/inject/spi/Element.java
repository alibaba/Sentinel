package com.google.inject.spi;

import com.google.inject.Binder;

public interface Element {
   Object getSource();

   <T> T acceptVisitor(ElementVisitor<T> var1);

   void applyTo(Binder var1);
}
