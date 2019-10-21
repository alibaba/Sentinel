package com.google.inject.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.ConfigurationException;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

import javax.inject.Provider;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class MoreTypes {
   public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
   private static final ImmutableMap<TypeLiteral<?>, TypeLiteral<?>> PRIMITIVE_TO_WRAPPER;

   private MoreTypes() {
   }

   public static <T> Key<T> canonicalizeKey(Key<T> key) {
      if (key.getClass() == Key.class) {
         return key;
      } else if (key.getAnnotation() != null) {
         return Key.get(key.getTypeLiteral(), key.getAnnotation());
      } else {
         return key.getAnnotationType() != null ? Key.get(key.getTypeLiteral(), key.getAnnotationType()) : Key.get(key.getTypeLiteral());
      }
   }

   public static <T> TypeLiteral<T> canonicalizeForKey(TypeLiteral<T> typeLiteral) {
      Type type = typeLiteral.getType();
      if (!isFullySpecified(type)) {
         Errors errors = (new Errors()).keyNotFullySpecified(typeLiteral);
         throw new ConfigurationException(errors.getMessages());
      } else {
         TypeLiteral recreated;
         if (typeLiteral.getRawType() == Provider.class) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            recreated = TypeLiteral.get((Type)Types.providerOf(parameterizedType.getActualTypeArguments()[0]));
            return recreated;
         } else {
            TypeLiteral<T> wrappedPrimitives = (TypeLiteral)PRIMITIVE_TO_WRAPPER.get(typeLiteral);
            if (wrappedPrimitives != null) {
               return wrappedPrimitives;
            } else if (typeLiteral.getClass() == TypeLiteral.class) {
               return typeLiteral;
            } else {
               recreated = TypeLiteral.get(typeLiteral.getType());
               return recreated;
            }
         }
      }
   }

   private static boolean isFullySpecified(Type type) {
      if (type instanceof Class) {
         return true;
      } else if (type instanceof CompositeType) {
         return ((CompositeType)type).isFullySpecified();
      } else {
         return type instanceof TypeVariable ? false : ((CompositeType)canonicalize(type)).isFullySpecified();
      }
   }

   public static Type canonicalize(Type type) {
      if (type instanceof Class) {
         Class<?> c = (Class)type;
         return (Type)(c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c);
      } else if (type instanceof CompositeType) {
         return type;
      } else if (type instanceof ParameterizedType) {
         ParameterizedType p = (ParameterizedType)type;
         return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
      } else if (type instanceof GenericArrayType) {
         GenericArrayType g = (GenericArrayType)type;
         return new GenericArrayTypeImpl(g.getGenericComponentType());
      } else if (type instanceof WildcardType) {
         WildcardType w = (WildcardType)type;
         return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
      } else {
         return type;
      }
   }

   public static Class<?> getRawType(Type type) {
      if (type instanceof Class) {
         return (Class)type;
      } else if (type instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType)type;
         Type rawType = parameterizedType.getRawType();
         Preconditions.checkArgument(rawType instanceof Class, "Expected a Class, but <%s> is of type %s", type, type.getClass().getName());
         return (Class)rawType;
      } else if (type instanceof GenericArrayType) {
         Type componentType = ((GenericArrayType)type).getGenericComponentType();
         return Array.newInstance(getRawType(componentType), 0).getClass();
      } else if (!(type instanceof TypeVariable) && !(type instanceof WildcardType)) {
         throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
      } else {
         return Object.class;
      }
   }

   public static boolean equals(Type a, Type b) {
      if (a == b) {
         return true;
      } else if (a instanceof Class) {
         return a.equals(b);
      } else if (a instanceof ParameterizedType) {
         if (!(b instanceof ParameterizedType)) {
            return false;
         } else {
            ParameterizedType pa = (ParameterizedType)a;
            ParameterizedType pb = (ParameterizedType)b;
            return Objects.equal(pa.getOwnerType(), pb.getOwnerType()) && pa.getRawType().equals(pb.getRawType()) && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
         }
      } else if (a instanceof GenericArrayType) {
         if (!(b instanceof GenericArrayType)) {
            return false;
         } else {
            GenericArrayType ga = (GenericArrayType)a;
            GenericArrayType gb = (GenericArrayType)b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());
         }
      } else if (a instanceof WildcardType) {
         if (!(b instanceof WildcardType)) {
            return false;
         } else {
            WildcardType wa = (WildcardType)a;
            WildcardType wb = (WildcardType)b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds()) && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());
         }
      } else if (a instanceof TypeVariable) {
         if (!(b instanceof TypeVariable)) {
            return false;
         } else {
            TypeVariable<?> va = (TypeVariable)a;
            TypeVariable<?> vb = (TypeVariable)b;
            return va.getGenericDeclaration().equals(vb.getGenericDeclaration()) && va.getName().equals(vb.getName());
         }
      } else {
         return false;
      }
   }

   private static int hashCodeOrZero(Object o) {
      return o != null ? o.hashCode() : 0;
   }

   public static String typeToString(Type type) {
      return type instanceof Class ? ((Class)type).getName() : type.toString();
   }

   public static Type getGenericSupertype(Type type, Class<?> rawType, Class<?> toResolve) {
      if (toResolve == rawType) {
         return type;
      } else {
         if (toResolve.isInterface()) {
            Class[] interfaces = rawType.getInterfaces();
            int i = 0;

            for(int length = interfaces.length; i < length; ++i) {
               if (interfaces[i] == toResolve) {
                  return rawType.getGenericInterfaces()[i];
               }

               if (toResolve.isAssignableFrom(interfaces[i])) {
                  return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
               }
            }
         }

         if (!rawType.isInterface()) {
            while(rawType != Object.class) {
               Class<?> rawSupertype = rawType.getSuperclass();
               if (rawSupertype == toResolve) {
                  return rawType.getGenericSuperclass();
               }

               if (toResolve.isAssignableFrom(rawSupertype)) {
                  return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
               }

               rawType = rawSupertype;
            }
         }

         return toResolve;
      }
   }

   public static Type resolveTypeVariable(Type type, Class<?> rawType, TypeVariable unknown) {
      Class<?> declaredByRaw = declaringClassOf(unknown);
      if (declaredByRaw == null) {
         return unknown;
      } else {
         Type declaredBy = getGenericSupertype(type, rawType, declaredByRaw);
         if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType)declaredBy).getActualTypeArguments()[index];
         } else {
            return unknown;
         }
      }
   }

   private static int indexOf(Object[] array, Object toFind) {
      for(int i = 0; i < array.length; ++i) {
         if (toFind.equals(array[i])) {
            return i;
         }
      }

      throw new NoSuchElementException();
   }

   private static Class<?> declaringClassOf(TypeVariable typeVariable) {
      GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
      return genericDeclaration instanceof Class ? (Class)genericDeclaration : null;
   }

   private static void checkNotPrimitive(Type type, String use) {
      Preconditions.checkArgument(!(type instanceof Class) || !((Class)type).isPrimitive(), "Primitive types are not allowed in %s: %s", use, type);
   }

   static {
      PRIMITIVE_TO_WRAPPER = (new Builder()).put(TypeLiteral.get(Boolean.TYPE), TypeLiteral.get(Boolean.class)).put(TypeLiteral.get(Byte.TYPE), TypeLiteral.get(Byte.class)).put(TypeLiteral.get(Short.TYPE), TypeLiteral.get(Short.class)).put(TypeLiteral.get(Integer.TYPE), TypeLiteral.get(Integer.class)).put(TypeLiteral.get(Long.TYPE), TypeLiteral.get(Long.class)).put(TypeLiteral.get(Float.TYPE), TypeLiteral.get(Float.class)).put(TypeLiteral.get(Double.TYPE), TypeLiteral.get(Double.class)).put(TypeLiteral.get(Character.TYPE), TypeLiteral.get(Character.class)).put(TypeLiteral.get(Void.TYPE), TypeLiteral.get(Void.class)).build();
   }

   private interface CompositeType {
      boolean isFullySpecified();
   }

   public static class WildcardTypeImpl implements WildcardType, Serializable, CompositeType {
      private final Type upperBound;
      private final Type lowerBound;
      private static final long serialVersionUID = 0L;

      public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
         Preconditions.checkArgument(lowerBounds.length <= 1, "Must have at most one lower bound.");
         Preconditions.checkArgument(upperBounds.length == 1, "Must have exactly one upper bound.");
         if (lowerBounds.length == 1) {
            Preconditions.checkNotNull(lowerBounds[0], "lowerBound");
            MoreTypes.checkNotPrimitive(lowerBounds[0], "wildcard bounds");
            Preconditions.checkArgument(upperBounds[0] == Object.class, "bounded both ways");
            this.lowerBound = MoreTypes.canonicalize(lowerBounds[0]);
            this.upperBound = Object.class;
         } else {
            Preconditions.checkNotNull(upperBounds[0], "upperBound");
            MoreTypes.checkNotPrimitive(upperBounds[0], "wildcard bounds");
            this.lowerBound = null;
            this.upperBound = MoreTypes.canonicalize(upperBounds[0]);
         }

      }

      public Type[] getUpperBounds() {
         return new Type[]{this.upperBound};
      }

      public Type[] getLowerBounds() {
         return this.lowerBound != null ? new Type[]{this.lowerBound} : MoreTypes.EMPTY_TYPE_ARRAY;
      }

      public boolean isFullySpecified() {
         return MoreTypes.isFullySpecified(this.upperBound) && (this.lowerBound == null || MoreTypes.isFullySpecified(this.lowerBound));
      }

      public boolean equals(Object other) {
         return other instanceof WildcardType && MoreTypes.equals(this, (WildcardType)other);
      }

      public int hashCode() {
         return (this.lowerBound != null ? 31 + this.lowerBound.hashCode() : 1) ^ 31 + this.upperBound.hashCode();
      }

      public String toString() {
         if (this.lowerBound != null) {
            return "? super " + MoreTypes.typeToString(this.lowerBound);
         } else {
            return this.upperBound == Object.class ? "?" : "? extends " + MoreTypes.typeToString(this.upperBound);
         }
      }
   }

   public static class GenericArrayTypeImpl implements GenericArrayType, Serializable, CompositeType {
      private final Type componentType;
      private static final long serialVersionUID = 0L;

      public GenericArrayTypeImpl(Type componentType) {
         this.componentType = MoreTypes.canonicalize(componentType);
      }

      public Type getGenericComponentType() {
         return this.componentType;
      }

      public boolean isFullySpecified() {
         return MoreTypes.isFullySpecified(this.componentType);
      }

      public boolean equals(Object o) {
         return o instanceof GenericArrayType && MoreTypes.equals(this, (GenericArrayType)o);
      }

      public int hashCode() {
         return this.componentType.hashCode();
      }

      public String toString() {
         return MoreTypes.typeToString(this.componentType) + "[]";
      }
   }

   public static class ParameterizedTypeImpl implements ParameterizedType, Serializable, CompositeType {
      private final Type ownerType;
      private final Type rawType;
      private final Type[] typeArguments;
      private static final long serialVersionUID = 0L;

      public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
         ensureOwnerType(ownerType, rawType);
         this.ownerType = ownerType == null ? null : MoreTypes.canonicalize(ownerType);
         this.rawType = MoreTypes.canonicalize(rawType);
         this.typeArguments = (Type[])typeArguments.clone();

         for(int t = 0; t < this.typeArguments.length; ++t) {
            Preconditions.checkNotNull(this.typeArguments[t], "type parameter");
            MoreTypes.checkNotPrimitive(this.typeArguments[t], "type parameters");
            this.typeArguments[t] = MoreTypes.canonicalize(this.typeArguments[t]);
         }

      }

      public Type[] getActualTypeArguments() {
         return (Type[])this.typeArguments.clone();
      }

      public Type getRawType() {
         return this.rawType;
      }

      public Type getOwnerType() {
         return this.ownerType;
      }

      public boolean isFullySpecified() {
         if (this.ownerType != null && !MoreTypes.isFullySpecified(this.ownerType)) {
            return false;
         } else if (!MoreTypes.isFullySpecified(this.rawType)) {
            return false;
         } else {
            Type[] arr$ = this.typeArguments;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Type type = arr$[i$];
               if (!MoreTypes.isFullySpecified(type)) {
                  return false;
               }
            }

            return true;
         }
      }

      public boolean equals(Object other) {
         return other instanceof ParameterizedType && MoreTypes.equals(this, (ParameterizedType)other);
      }

      public int hashCode() {
         return Arrays.hashCode(this.typeArguments) ^ this.rawType.hashCode() ^ MoreTypes.hashCodeOrZero(this.ownerType);
      }

      public String toString() {
         StringBuilder stringBuilder = new StringBuilder(30 * (this.typeArguments.length + 1));
         stringBuilder.append(MoreTypes.typeToString(this.rawType));
         if (this.typeArguments.length == 0) {
            return stringBuilder.toString();
         } else {
            stringBuilder.append("<").append(MoreTypes.typeToString(this.typeArguments[0]));

            for(int i = 1; i < this.typeArguments.length; ++i) {
               stringBuilder.append(", ").append(MoreTypes.typeToString(this.typeArguments[i]));
            }

            return stringBuilder.append(">").toString();
         }
      }

      private static void ensureOwnerType(Type ownerType, Type rawType) {
         if (rawType instanceof Class) {
            Class rawTypeAsClass = (Class)rawType;
            Preconditions.checkArgument(ownerType != null || rawTypeAsClass.getEnclosingClass() == null, "No owner type for enclosed %s", rawType);
            Preconditions.checkArgument(ownerType == null || rawTypeAsClass.getEnclosingClass() != null, "Owner type for unenclosed %s", rawType);
         }

      }
   }
}
