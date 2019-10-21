package com.google.inject.internal;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapKey;
import com.google.inject.multibindings.ProvidesIntoMap;
import com.google.inject.multibindings.ProvidesIntoOptional;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ModuleAnnotatedMethodScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

final class ProvidesMethodScanner extends ModuleAnnotatedMethodScanner {
   static final ProvidesMethodScanner INSTANCE = new ProvidesMethodScanner();
   private static final ImmutableSet<Class<? extends Annotation>> ANNOTATIONS = ImmutableSet.of(Provides.class, ProvidesIntoSet.class, ProvidesIntoMap.class, ProvidesIntoOptional.class);

   private ProvidesMethodScanner() {
   }

   public Set<? extends Class<? extends Annotation>> annotationClasses() {
      return ANNOTATIONS;
   }

   public <T> Key<T> prepareMethod(Binder binder, Annotation annotation, Key<T> key, InjectionPoint injectionPoint) {
      Method method = (Method)injectionPoint.getMember();
      AnnotationOrError mapKey = findMapKeyAnnotation(binder, method);
      if (annotation instanceof Provides) {
         if (mapKey.annotation != null) {
            binder.addError("Found a MapKey annotation on non map binding at %s.", method);
         }

         return key;
      } else if (annotation instanceof ProvidesIntoSet) {
         if (mapKey.annotation != null) {
            binder.addError("Found a MapKey annotation on non map binding at %s.", method);
         }

         return RealMultibinder.newRealSetBinder(binder, key).getKeyForNewItem();
      } else if (annotation instanceof ProvidesIntoMap) {
         if (mapKey.error) {
            return key;
         } else if (mapKey.annotation == null) {
            binder.addError("No MapKey found for map binding at %s.", method);
            return key;
         } else {
            TypeAndValue typeAndValue = typeAndValueOfMapKey(mapKey.annotation);
            return RealMapBinder.newRealMapBinder(binder, typeAndValue.type, key).getKeyForNewValue(typeAndValue.value);
         }
      } else {
         if (annotation instanceof ProvidesIntoOptional) {
            if (mapKey.annotation != null) {
               binder.addError("Found a MapKey annotation on non map binding at %s.", method);
            }

            switch(((ProvidesIntoOptional)annotation).value()) {
            case DEFAULT:
               return RealOptionalBinder.newRealOptionalBinder(binder, key).getKeyForDefaultBinding();
            case ACTUAL:
               return RealOptionalBinder.newRealOptionalBinder(binder, key).getKeyForActualBinding();
            }
         }

         throw new IllegalStateException("Invalid annotation: " + annotation);
      }
   }

   private static AnnotationOrError findMapKeyAnnotation(Binder binder, Method method) {
      Annotation foundAnnotation = null;
      Annotation[] arr$ = method.getAnnotations();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation annotation = arr$[i$];
         MapKey mapKey = (MapKey)annotation.annotationType().getAnnotation(MapKey.class);
         if (mapKey != null) {
            if (foundAnnotation != null) {
               binder.addError("Found more than one MapKey annotations on %s.", method);
               return AnnotationOrError.forError();
            }

            if (mapKey.unwrapValue()) {
               try {
                  Method valueMethod = annotation.annotationType().getDeclaredMethod("value");
                  if (valueMethod.getReturnType().isArray()) {
                     binder.addError("Array types are not allowed in a MapKey with unwrapValue=true: %s", annotation.annotationType());
                     return AnnotationOrError.forError();
                  }
               } catch (NoSuchMethodException var9) {
                  binder.addError("No 'value' method in MapKey with unwrapValue=true: %s", annotation.annotationType());
                  return AnnotationOrError.forError();
               }
            }

            foundAnnotation = annotation;
         }
      }

      return AnnotationOrError.forPossiblyNullAnnotation(foundAnnotation);
   }

   static TypeAndValue<?> typeAndValueOfMapKey(Annotation mapKeyAnnotation) {
      if (!((MapKey)mapKeyAnnotation.annotationType().getAnnotation(MapKey.class)).unwrapValue()) {
         return new TypeAndValue(TypeLiteral.get(mapKeyAnnotation.annotationType()), mapKeyAnnotation);
      } else {
         try {
            Method valueMethod = mapKeyAnnotation.annotationType().getDeclaredMethod("value");
            valueMethod.setAccessible(true);
            TypeLiteral<?> returnType = TypeLiteral.get(mapKeyAnnotation.annotationType()).getReturnType(valueMethod);
            return new TypeAndValue(returnType, valueMethod.invoke(mapKeyAnnotation));
         } catch (NoSuchMethodException var3) {
            throw new IllegalStateException(var3);
         } catch (SecurityException var4) {
            throw new IllegalStateException(var4);
         } catch (IllegalAccessException var5) {
            throw new IllegalStateException(var5);
         } catch (InvocationTargetException var6) {
            throw new IllegalStateException(var6);
         }
      }
   }

   private static class TypeAndValue<T> {
      final TypeLiteral<T> type;
      final T value;

      TypeAndValue(TypeLiteral<T> type, T value) {
         this.type = type;
         this.value = value;
      }
   }

   private static class AnnotationOrError {
      final Annotation annotation;
      final boolean error;

      AnnotationOrError(Annotation annotation, boolean error) {
         this.annotation = annotation;
         this.error = error;
      }

      static AnnotationOrError forPossiblyNullAnnotation(Annotation annotation) {
         return new AnnotationOrError(annotation, false);
      }

      static AnnotationOrError forError() {
         return new AnnotationOrError((Annotation)null, true);
      }
   }
}
