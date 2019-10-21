/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.internal;

import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.SourceProvider;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Handles {@code Binder.convertToTypes} commands.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 */
final class TypeConverterBindingProcessor extends AbstractProcessor {

   TypeConverterBindingProcessor(Errors errors) {
      super(errors);
   }

   /** Installs default converters for primitives, enums, and class literals. */
   static void prepareBuiltInConverters(InjectorImpl injector) {
      // Configure type converters.
      convertToPrimitiveType(injector, int.class, Integer.class);
      convertToPrimitiveType(injector, long.class, Long.class);
      convertToPrimitiveType(injector, boolean.class, Boolean.class);
      convertToPrimitiveType(injector, byte.class, Byte.class);
      convertToPrimitiveType(injector, short.class, Short.class);
      convertToPrimitiveType(injector, float.class, Float.class);
      convertToPrimitiveType(injector, double.class, Double.class);

      convertToClass(
              injector,
              Character.class,
              new TypeConverter() {
                 @Override
                 public Object convert(String value, TypeLiteral<?> toType) {
                    value = value.trim();
                    if (value.length() != 1) {
                       throw new RuntimeException("Length != 1.");
                    }
                    return value.charAt(0);
                 }

                 @Override
                 public String toString() {
                    return "TypeConverter<Character>";
                 }
              });

      convertToClasses(
              injector,
              Matchers.subclassesOf(Enum.class),
              new TypeConverter() {
                 @Override
                 @SuppressWarnings("unchecked")
                 public Object convert(String value, TypeLiteral<?> toType) {
                    return Enum.valueOf((Class) toType.getRawType(), value);
                 }

                 @Override
                 public String toString() {
                    return "TypeConverter<E extends Enum<E>>";
                 }
              });

      internalConvertToTypes(
              injector,
              new AbstractMatcher<TypeLiteral<?>>() {
                 @Override
                 public boolean matches(TypeLiteral<?> typeLiteral) {
                    return typeLiteral.getRawType() == Class.class;
                 }

                 @Override
                 public String toString() {
                    return "Class<?>";
                 }
              },
              new TypeConverter() {
                 @Override
                 @SuppressWarnings("unchecked")
                 public Object convert(String value, TypeLiteral<?> toType) {
                    try {
                       return Class.forName(value);
                    } catch (ClassNotFoundException e) {
                       throw new RuntimeException(e.getMessage());
                    }
                 }

                 @Override
                 public String toString() {
                    return "TypeConverter<Class<?>>";
                 }
              });
   }

   private static <T> void convertToPrimitiveType(
           InjectorImpl injector, Class<T> primitiveType, final Class<T> wrapperType) {
      try {
         final Method parser =
                 wrapperType.getMethod("parse" + capitalize(primitiveType.getName()), String.class);

         TypeConverter typeConverter =
                 new TypeConverter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Object convert(String value, TypeLiteral<?> toType) {
                       try {
                          return parser.invoke(null, value);
                       } catch (IllegalAccessException e) {
                          throw new AssertionError(e);
                       } catch (InvocationTargetException e) {
                          throw new RuntimeException(e.getTargetException().getMessage());
                       }
                    }

                    @Override
                    public String toString() {
                       return "TypeConverter<" + wrapperType.getSimpleName() + ">";
                    }
                 };

         convertToClass(injector, wrapperType, typeConverter);
      } catch (NoSuchMethodException e) {
         throw new AssertionError(e);
      }
   }

   private static <T> void convertToClass(
           InjectorImpl injector, Class<T> type, TypeConverter converter) {
      convertToClasses(injector, Matchers.identicalTo(type), converter);
   }

   private static void convertToClasses(
           InjectorImpl injector, final Matcher<? super Class<?>> typeMatcher, TypeConverter converter) {
      internalConvertToTypes(
              injector,
              new AbstractMatcher<TypeLiteral<?>>() {
                 @Override
                 public boolean matches(TypeLiteral<?> typeLiteral) {
                    Type type = typeLiteral.getType();
                    if (!(type instanceof Class)) {
                       return false;
                    }
                    Class<?> clazz = (Class<?>) type;
                    return typeMatcher.matches(clazz);
                 }

                 @Override
                 public String toString() {
                    return typeMatcher.toString();
                 }
              },
              converter);
   }

   private static void internalConvertToTypes(
           InjectorImpl injector, Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
      injector.state.addConverter(
              new TypeConverterBinding(SourceProvider.UNKNOWN_SOURCE, typeMatcher, converter));
   }

   @Override
   public Boolean visit(TypeConverterBinding command) {
      injector.state.addConverter(
              new TypeConverterBinding(
                      command.getSource(), command.getTypeMatcher(), command.getTypeConverter()));
      return true;
   }

   private static String capitalize(String s) {
      if (s.length() == 0) {
         return s;
      }
      char first = s.charAt(0);
      char capitalized = Character.toUpperCase(first);
      return (first == capitalized) ? s : capitalized + s.substring(1);
   }
}
