package com.google.inject.matcher;

public interface Matcher<T> {
   boolean matches(T var1);

   Matcher<T> and(Matcher<? super T> var1);

   Matcher<T> or(Matcher<? super T> var1);
}
