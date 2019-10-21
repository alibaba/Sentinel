package com.google.inject;

public interface Provider<T> extends javax.inject.Provider<T> {
   T get();
}
