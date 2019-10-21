package com.google.inject;

public interface Scope {
   <T> Provider<T> scope(Key<T> var1, Provider<T> var2);

   String toString();
}
