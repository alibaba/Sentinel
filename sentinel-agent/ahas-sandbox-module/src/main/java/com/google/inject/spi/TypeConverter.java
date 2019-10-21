package com.google.inject.spi;

import com.google.inject.TypeLiteral;

public interface TypeConverter {
   Object convert(String var1, TypeLiteral<?> var2);
}
