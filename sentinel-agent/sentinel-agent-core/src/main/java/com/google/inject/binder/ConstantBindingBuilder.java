package com.google.inject.binder;

public interface ConstantBindingBuilder {
   void to(String var1);

   void to(int var1);

   void to(long var1);

   void to(boolean var1);

   void to(double var1);

   void to(float var1);

   void to(short var1);

   void to(char var1);

   void to(byte var1);

   void to(Class<?> var1);

   <E extends Enum<E>> void to(E var1);
}
