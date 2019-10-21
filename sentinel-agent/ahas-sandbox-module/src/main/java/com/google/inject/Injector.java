package com.google.inject;

import com.google.inject.spi.TypeConverterBinding;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Injector {
   void injectMembers(Object var1);

   <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> var1);

   <T> MembersInjector<T> getMembersInjector(Class<T> var1);

   Map<Key<?>, Binding<?>> getBindings();

   Map<Key<?>, Binding<?>> getAllBindings();

   <T> Binding<T> getBinding(Key<T> var1);

   <T> Binding<T> getBinding(Class<T> var1);

   <T> Binding<T> getExistingBinding(Key<T> var1);

   <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> var1);

   <T> Provider<T> getProvider(Key<T> var1);

   <T> Provider<T> getProvider(Class<T> var1);

   <T> T getInstance(Key<T> var1);

   <T> T getInstance(Class<T> var1);

   Injector getParent();

   Injector createChildInjector(Iterable<? extends Module> var1);

   Injector createChildInjector(Module... var1);

   Map<Class<? extends Annotation>, Scope> getScopeBindings();

   Set<TypeConverterBinding> getTypeConverterBindings();
}
