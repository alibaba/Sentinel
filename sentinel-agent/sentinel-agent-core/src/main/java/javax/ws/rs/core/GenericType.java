package javax.ws.rs.core;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Stack;

public class GenericType<T> {
   private final Type type;
   private final Class<?> rawType;

   public static GenericType forInstance(Object instance) {
      GenericType genericType;
      if (instance instanceof GenericEntity) {
         genericType = new GenericType(((GenericEntity)instance).getType());
      } else {
         genericType = instance == null ? null : new GenericType(instance.getClass());
      }

      return genericType;
   }

   protected GenericType() {
      this.type = getTypeArgument(this.getClass(), GenericType.class);
      this.rawType = getClass(this.type);
   }

   public GenericType(Type genericType) {
      if (genericType == null) {
         throw new IllegalArgumentException("Type must not be null");
      } else {
         this.type = genericType;
         this.rawType = getClass(this.type);
      }
   }

   public final Type getType() {
      return this.type;
   }

   public final Class<?> getRawType() {
      return this.rawType;
   }

   private static Class getClass(Type type) {
      if (type instanceof Class) {
         return (Class)type;
      } else {
         if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            if (parameterizedType.getRawType() instanceof Class) {
               return (Class)parameterizedType.getRawType();
            }
         } else if (type instanceof GenericArrayType) {
            GenericArrayType array = (GenericArrayType)type;
            Class<?> componentRawType = getClass(array.getGenericComponentType());
            return getArrayClass(componentRawType);
         }

         throw new IllegalArgumentException("Type parameter " + type.toString() + " not a class or parameterized type whose raw type is a class");
      }
   }

   private static Class getArrayClass(Class c) {
      try {
         Object o = Array.newInstance(c, 0);
         return o.getClass();
      } catch (Exception var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   static Type getTypeArgument(Class<?> clazz, Class<?> baseClass) {
      Stack<Type> superclasses = new Stack();
      Class currentClass = clazz;

      Type currentType;
      do {
         currentType = currentClass.getGenericSuperclass();
         superclasses.push(currentType);
         if (currentType instanceof Class) {
            currentClass = (Class)currentType;
         } else if (currentType instanceof ParameterizedType) {
            currentClass = (Class)((ParameterizedType)currentType).getRawType();
         }
      } while(!currentClass.equals(baseClass));

      Type typeArg;
      for(TypeVariable tv = baseClass.getTypeParameters()[0]; !superclasses.isEmpty(); tv = (TypeVariable)typeArg) {
         currentType = (Type)superclasses.pop();
         if (!(currentType instanceof ParameterizedType)) {
            break;
         }

         ParameterizedType pt = (ParameterizedType)currentType;
         Class<?> rawType = (Class)pt.getRawType();
         int argIndex = Arrays.asList(rawType.getTypeParameters()).indexOf(tv);
         if (argIndex <= -1) {
            break;
         }

         typeArg = pt.getActualTypeArguments()[argIndex];
         if (!(typeArg instanceof TypeVariable)) {
            return typeArg;
         }
      }

      throw new IllegalArgumentException(currentType + " does not specify the type parameter T of GenericType<T>");
   }

   public boolean equals(Object obj) {
      boolean result = this == obj;
      if (!result && obj instanceof GenericType) {
         GenericType<?> that = (GenericType)obj;
         return this.type.equals(that.type);
      } else {
         return result;
      }
   }

   public int hashCode() {
      return this.type.hashCode();
   }

   public String toString() {
      return "GenericType{" + this.type.toString() + "}";
   }
}
