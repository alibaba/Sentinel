package com.alibaba.jvm.sandbox.core.util.matcher.structure;

import com.alibaba.jvm.sandbox.api.util.LazyGet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

abstract class ModifierAccess implements Access {

    private final int modifiers;

    ModifierAccess(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    @Override
    public boolean isInterface() {
        return Modifier.isInterface(modifiers);
    }

    @Override
    public boolean isNative() {
        return Modifier.isNative(modifiers);
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

}

class AccessImplByJDKClass extends ModifierAccess {

    private final Class<?> clazz;

    AccessImplByJDKClass(Class<?> clazz) {
        super(clazz.getModifiers());
        this.clazz = clazz;
    }

    @Override
    public boolean isEnum() {
        return clazz.isEnum();
    }

    @Override
    public boolean isAnnotation() {
        return clazz.isAnnotation();
    }

}

class AccessImplByJDKBehavior extends ModifierAccess {

    AccessImplByJDKBehavior(Method method) {
        super(method.getModifiers());
    }

    AccessImplByJDKBehavior(Constructor constructor) {
        super(constructor.getModifiers());
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isAnnotation() {
        return false;
    }
}

/**
 * 用JDK的反射实现的类结构
 */
public class ClassStructureImplByJDK extends FamilyClassStructure {

    private final Class<?> clazz;
    private String javaClassName;

    public ClassStructureImplByJDK(final Class<?> clazz) {
        this.clazz = clazz;
    }

    private ClassStructure newInstance(final Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        return new ClassStructureImplByJDK(clazz);
    }

    private List<ClassStructure> newInstances(final Class[] classArray) {
        final List<ClassStructure> classStructures = new ArrayList<ClassStructure>();
        if (null != classArray) {
            for (final Class<?> clazz : classArray) {
                final ClassStructure classStructure = newInstance(clazz);
                if (null != classStructure) {
                    classStructures.add(classStructure);
                }
            }
        }
        return classStructures;
    }

    @Override
    public String getJavaClassName() {
        return null != javaClassName
                ? javaClassName
                : (javaClassName = getJavaClassName(clazz));
    }

    private String getJavaClassName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }


    @Override
    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }

    @Override
    public ClassStructure getSuperClassStructure() {
        // 过滤掉Object.class
        return Object.class.equals(clazz.getSuperclass())
                ? null
                : newInstance(clazz.getSuperclass());
    }

    @Override
    public List<ClassStructure> getInterfaceClassStructures() {
        return newInstances(clazz.getInterfaces());
    }

    private Class[] getAnnotationTypeArray(final Annotation[] annotationArray) {
        final Collection<Class> annotationTypes = new ArrayList<Class>();
        for (final Annotation annotation : annotationArray) {
            if (annotation.getClass().isAnnotation()) {
                annotationTypes.add(annotation.getClass());
            }
            for (final Class annotationInterfaceClass : annotation.getClass().getInterfaces()) {
                if (annotationInterfaceClass.isAnnotation()) {
                    annotationTypes.add(annotationInterfaceClass);
                }
            }
        }
        return annotationTypes.toArray(new Class[0]);
    }

    private final LazyGet<List<ClassStructure>> annotationTypeClassStructuresLazyGet
            = new LazyGet<List<ClassStructure>>() {
        @Override
        protected List<ClassStructure> initialValue() {
            return Collections.unmodifiableList(newInstances(getAnnotationTypeArray(clazz.getDeclaredAnnotations())));
        }
    };

    @Override
    public List<ClassStructure> getAnnotationTypeClassStructures() {
        return annotationTypeClassStructuresLazyGet.get();
    }

    private BehaviorStructure newBehaviorStructure(final Method method) {
        return new BehaviorStructure(
                new AccessImplByJDKBehavior(method),
                method.getName(),
                this,
                newInstance(method.getReturnType()),
                newInstances(method.getParameterTypes()),
                newInstances(method.getExceptionTypes()),
                newInstances(getAnnotationTypeArray(method.getDeclaredAnnotations()))
        );
    }

    private BehaviorStructure newBehaviorStructure(final Constructor constructor) {
        return new BehaviorStructure(
                new AccessImplByJDKBehavior(constructor),
                "<init>",
                this,
                this,
                newInstances(constructor.getParameterTypes()),
                newInstances(constructor.getExceptionTypes()),
                newInstances(getAnnotationTypeArray(constructor.getDeclaredAnnotations()))
        );
    }

    private final LazyGet<List<BehaviorStructure>> behaviorStructuresLazyGet
            = new LazyGet<List<BehaviorStructure>>() {
        @Override
        protected List<BehaviorStructure> initialValue() {
            final List<BehaviorStructure> behaviorStructures = new ArrayList<BehaviorStructure>();
            for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                behaviorStructures.add(newBehaviorStructure(constructor));
            }
            for (final Method method : clazz.getDeclaredMethods()) {
                behaviorStructures.add(newBehaviorStructure(method));
            }
            return Collections.unmodifiableList(behaviorStructures);
        }
    };

    @Override
    public List<BehaviorStructure> getBehaviorStructures() {
        return behaviorStructuresLazyGet.get();
    }

    @Override
    public Access getAccess() {
        return new AccessImplByJDKClass(clazz);
    }

    @Override
    public String toString() {
        return "ClassStructureImplByJDK{" + "javaClassName='" + javaClassName + '\'' + '}';
    }
}
