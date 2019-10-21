package com.google.inject.internal;

final class Initializables {
   static <T> com.google.inject.internal.Initializable<T> of(final T instance) {
      return new Initializable<T>() {
         public T get() {
            return instance;
         }

         public String toString() {
            return String.valueOf(instance);
         }
      };
   }
}
