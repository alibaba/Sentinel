package com.google.inject.spi;

import com.google.inject.Binding;
import java.util.Set;

public interface ConstructorBinding<T> extends Binding<T>, HasDependencies {
   InjectionPoint getConstructor();

   Set<InjectionPoint> getInjectableMembers();
}
