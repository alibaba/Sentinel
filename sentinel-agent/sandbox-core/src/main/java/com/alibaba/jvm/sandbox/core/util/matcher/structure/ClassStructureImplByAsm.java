package com.alibaba.jvm.sandbox.core.util.matcher.structure;

import com.alibaba.jvm.sandbox.api.util.LazyGet;
import com.alibaba.jvm.sandbox.core.util.BitUtils;
import com.alibaba.jvm.sandbox.core.util.collection.Pair;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.PrimitiveClassStructure.Primitive;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.jvm.sandbox.core.util.SandboxStringUtils.toInternalClassName;
import static com.alibaba.jvm.sandbox.core.util.SandboxStringUtils.toJavaClassName;
import static com.alibaba.jvm.sandbox.core.util.matcher.structure.PrimitiveClassStructure.mappingPrimitiveByJavaClassName;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * {@link Access}的ASM实现
 */
class AccessImplByAsm implements Access {

    private final int access;

    AccessImplByAsm(final int access) {
        this.access = access;
    }

    /**
     * 获取Class的ACC位码
     * <p>
     * ACC位码是一个int类型的BITMAP，
     * 参考{@link Opcodes}的{@code access flags}片段
     * </p>
     *
     * @return ACC位码
     */
    private int getAccess() {
        return access;
    }

    @Override
    public boolean isPublic() {
        return BitUtils.isIn(getAccess(), ACC_PUBLIC);
    }

    @Override
    public boolean isPrivate() {
        return BitUtils.isIn(getAccess(), ACC_PRIVATE);
    }

    @Override
    public boolean isProtected() {
        return BitUtils.isIn(getAccess(), ACC_PROTECTED);
    }

    @Override
    public boolean isStatic() {
        // 隐性的Java语法约束：如果是接口类型，就一定是静态的
        return isInterface()
                || BitUtils.isIn(getAccess(), ACC_STATIC);
    }

    @Override
    public boolean isFinal() {
        return BitUtils.isIn(getAccess(), ACC_FINAL);
    }

    @Override
    public boolean isInterface() {
        return BitUtils.isIn(getAccess(), ACC_INTERFACE);
    }

    @Override
    public boolean isNative() {
        return BitUtils.isIn(getAccess(), ACC_NATIVE);
    }

    @Override
    public boolean isAbstract() {
        return BitUtils.isIn(getAccess(), ACC_ABSTRACT);
    }

    @Override
    public boolean isEnum() {
        return BitUtils.isIn(getAccess(), ACC_ENUM);
    }

    @Override
    public boolean isAnnotation() {
        return BitUtils.isIn(getAccess(), ACC_ANNOTATION);
    }
}

class EmptyClassStructure implements ClassStructure {

    @Override
    public String getJavaClassName() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public ClassStructure getSuperClassStructure() {
        return null;
    }

    @Override
    public List<ClassStructure> getInterfaceClassStructures() {
        return Collections.emptyList();
    }

    @Override
    public LinkedHashSet<ClassStructure> getFamilySuperClassStructures() {
        return new LinkedHashSet<ClassStructure>();
    }

    @Override
    public Set<ClassStructure> getFamilyInterfaceClassStructures() {
        return Collections.emptySet();
    }

    @Override
    public Set<ClassStructure> getFamilyTypeClassStructures() {
        return Collections.emptySet();
    }

    @Override
    public List<ClassStructure> getAnnotationTypeClassStructures() {
        return Collections.emptyList();
    }

    @Override
    public Set<ClassStructure> getFamilyAnnotationTypeClassStructures() {
        return Collections.emptySet();
    }

    @Override
    public List<BehaviorStructure> getBehaviorStructures() {
        return Collections.emptyList();
    }

    @Override
    public Access getAccess() {
        return new AccessImplByAsm(0);
    }
}

/**
 * JDK原生类型结构体
 */
class PrimitiveClassStructure extends EmptyClassStructure {

    private final Primitive primitive;

    PrimitiveClassStructure(Primitive primitive) {
        this.primitive = primitive;
    }

    public enum Primitive {
        BOOLEAN("boolean", boolean.class),
        CHAR("char", char.class),
        BYTE("byte", byte.class),
        INT("int", int.class),
        SHORT("short", short.class),
        LONG("long", long.class),
        FLOAT("float", float.class),
        DOUBLE("double", double.class),
        VOID("void", void.class);

        private final String type;
        private final Access access;

        Primitive(final String type, final Class<?> clazz) {
            this.type = type;
            this.access = new AccessImplByJDKClass(clazz);
        }
    }

    @Override
    public Access getAccess() {
        return primitive.access;
    }

    @Override
    public String getJavaClassName() {
        return primitive.type;
    }

    static Primitive mappingPrimitiveByJavaClassName(final String javaClassName) {
        for (final Primitive primitive : Primitive.values()) {
            if (primitive.type.equals(javaClassName)) {
                return primitive;
            }
        }
        return null;
    }

}

class ArrayClassStructure extends EmptyClassStructure {

    private final ClassStructure elementClassStructure;

    ArrayClassStructure(ClassStructure elementClassStructure) {
        this.elementClassStructure = elementClassStructure;
    }

    @Override
    public String getJavaClassName() {
        return elementClassStructure.getJavaClassName() + "[]";
    }
}

/**
 * 用ASM实现的类结构
 *
 * @author luanjia@taobao.com
 */
public class ClassStructureImplByAsm extends FamilyClassStructure {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ClassReader classReader;
    private final ClassLoader loader;
    private final Access access;

    ClassStructureImplByAsm(final InputStream classInputStream,
                            final ClassLoader loader) throws IOException {
        this(IOUtils.toByteArray(classInputStream), loader);
    }

    ClassStructureImplByAsm(final byte[] classByteArray,
                            final ClassLoader loader) {
        this.classReader = new ClassReader(classByteArray);
        this.loader = loader;
        this.access = fixAccess();
    }

    /**
     * 修正内部类时候Access的获取策略差异
     *
     * @return 修正后的Access
     */
    private Access fixAccess() {
        final AtomicInteger accessRef = new AtomicInteger(this.classReader.getAccess());
        final String internalClassName = this.classReader.getClassName();
        this.classReader.accept(new ClassVisitor(ASM7) {
            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                if (StringUtils.equals(name, internalClassName)) {
                    accessRef.set(access);
                }
            }
        }, ASM7);
        return new AccessImplByAsm(accessRef.get());
    }

    private boolean isBootstrapClassLoader() {
        return null == loader;
    }

    // 获取资源数据流
    // 一般而言可以从loader直接获取，如果获取不到那么这个类也会能加载成功
    // 但如果遇到来自BootstrapClassLoader的类就必须从java.lang.Object来获取
    private InputStream getResourceAsStream(final String resourceName) {
        return isBootstrapClassLoader()
                ? Object.class.getResourceAsStream("/" + resourceName)
                : loader.getResourceAsStream(resourceName);
    }

    // 将内部类名称转换为资源名称
    private String internalClassNameToResourceName(final String internalClassName) {
        return internalClassName + ".class";
    }

    private final static Cache<Pair, ClassStructure> classStructureCache
            = CacheBuilder.newBuilder().maximumSize(1024).build();

//    private final static GaLRUCache<Pair, ClassStructure> classStructureCache
//            = new GaLRUCache<Pair, ClassStructure>(1024);

    // 构造一个类结构实例
    private ClassStructure newInstance(final String javaClassName) {

        // 空载保护
        if (null == javaClassName) {
            return null;
        }

        // 是个数组类型
        if (javaClassName.endsWith("[]")) {
            return new ArrayClassStructure(newInstance(javaClassName.substring(0, javaClassName.length() - 2)));
        }

        // 是个基本类型
        final Primitive primitive = mappingPrimitiveByJavaClassName(javaClassName);
        if (null != primitive) {
            return new PrimitiveClassStructure(primitive);
        }

        final Pair pair = new Pair(loader, javaClassName);
        final ClassStructure existClassStructure = classStructureCache.getIfPresent(pair);
        if (null != existClassStructure) {
            return existClassStructure;
        } else {

            final InputStream is = getResourceAsStream(internalClassNameToResourceName(toInternalClassName(javaClassName)));
            if (null != is) {
                try {
                    final ClassStructure classStructure = new ClassStructureImplByAsm(is, loader);
                    classStructureCache.put(pair, classStructure);
                    return classStructure;
                } catch (Throwable cause) {
                    // ignore
                    logger.warn("new instance class structure by using ASM failed, will return null. class={};loader={};",
                            javaClassName, loader, cause);
                    classStructureCache.put(pair, null);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }

        // 出现异常或者找不到
        return null;
    }

    // 构造一个类结构实例数组
    private List<ClassStructure> newInstances(final String[] javaClassNameArray) {
        final List<ClassStructure> classStructures = new ArrayList<ClassStructure>();
        if (null == javaClassNameArray) {
            return classStructures;
        }
        for (final String javaClassName : javaClassNameArray) {
            final ClassStructure classStructure = newInstance(javaClassName);
            if (null != classStructure) {
                classStructures.add(classStructure);
            }
        }
        return classStructures;
    }

    // 遍历一个类结构
    private void accept(final ClassVisitor cv) {
        classReader.accept(cv, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
    }

    @Override
    public String getJavaClassName() {
        return toJavaClassName(classReader.getClassName());
    }

    @Override
    public ClassLoader getClassLoader() {
        return loader;
    }

    private final LazyGet<ClassStructure> superClassStructureLazyGet
            = new LazyGet<ClassStructure>() {
        @Override
        protected ClassStructure initialValue() {
            final String superInternalClassName = classReader.getSuperName();
            if (StringUtils.equals("java/lang/Object", superInternalClassName)) {
                return null;
            }
            return newInstance(toJavaClassName(superInternalClassName));
        }
    };

    @Override
    public ClassStructure getSuperClassStructure() {
        return superClassStructureLazyGet.get();
    }

    private final LazyGet<List<ClassStructure>> interfaceClassStructuresLazyGet
            = new LazyGet<List<ClassStructure>>() {
        @Override
        protected List<ClassStructure> initialValue() {
            return newInstances(classReader.getInterfaces());
        }
    };

    @Override
    public List<ClassStructure> getInterfaceClassStructures() {
        return interfaceClassStructuresLazyGet.get();
    }

    private final LazyGet<List<ClassStructure>> annotationTypeClassStructuresLazyGet
            = new LazyGet<List<ClassStructure>>() {
        @Override
        protected List<ClassStructure> initialValue() {
            final List<ClassStructure> annotationTypeClassStructures = new ArrayList<ClassStructure>();
            accept(new ClassVisitor(ASM7) {

                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (visible) {
                        final ClassStructure annotationTypeClassStructure = newInstance(Type.getType(desc).getClassName());
                        if (null != annotationTypeClassStructure) {
                            annotationTypeClassStructures.add(annotationTypeClassStructure);
                        }
                    }
                    return super.visitAnnotation(desc, visible);
                }

            });
            return annotationTypeClassStructures;
        }
    };

    @Override
    public List<ClassStructure> getAnnotationTypeClassStructures() {
        return annotationTypeClassStructuresLazyGet.get();
    }


    private final LazyGet<List<BehaviorStructure>> behaviorStructuresLazyGet
            = new LazyGet<List<BehaviorStructure>>() {
        @Override
        protected List<BehaviorStructure> initialValue() {
            final List<BehaviorStructure> behaviorStructures = new ArrayList<BehaviorStructure>();
            accept(new ClassVisitor(ASM7) {

                @Override
                public MethodVisitor visitMethod(final int access,
                                                 final String name,
                                                 final String desc,
                                                 final String signature,
                                                 final String[] exceptions) {

                    // 修复ASM会把<clinit>列入正常方法中的问题
                    // 实际上这个方法并不会参与到任何的逻辑判断
                    if (StringUtils.equals("<clinit>", name)) {
                        return super.visitMethod(access, name, desc, signature, exceptions);
                    }

                    return new MethodVisitor(ASM7, super.visitMethod(access, name, desc, signature, exceptions)) {

                        private final Type methodType = Type.getMethodType(desc);
                        private final List<ClassStructure> annotationTypeClassStructures = new ArrayList<ClassStructure>();

                        private String[] typeArrayToJavaClassNameArray(final Type[] typeArray) {
                            final List<String> javaClassNames = new ArrayList<String>();
                            if (null != typeArray) {
                                for (Type type : typeArray) {
                                    javaClassNames.add(type.getClassName());
                                }
                            }
                            return javaClassNames.toArray(new String[0]);
                        }

                        private List<ClassStructure> getParameterTypeClassStructures() {
                            return newInstances(
                                    typeArrayToJavaClassNameArray(methodType.getArgumentTypes())
                            );
                        }

                        private ClassStructure getReturnTypeClassStructure() {
                            if ("<init>".equals(name)) {
                                return ClassStructureImplByAsm.this;
                            } else {
                                final Type returnType = methodType.getReturnType();
                                return newInstance(returnType.getClassName());
                            }
                        }

                        @Override
                        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                            if (visible) {
                                final ClassStructure annotationTypeClassStructure = newInstance(Type.getType(desc).getClassName());
                                if (null != annotationTypeClassStructure) {
                                    annotationTypeClassStructures.add(annotationTypeClassStructure);
                                }
                            }
                            return super.visitAnnotation(desc, visible);
                        }

                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            final BehaviorStructure behaviorStructure = new BehaviorStructure(
                                    new AccessImplByAsm(access),
                                    name,
                                    ClassStructureImplByAsm.this,
                                    getReturnTypeClassStructure(),
                                    getParameterTypeClassStructures(),
                                    newInstances(exceptions),
                                    annotationTypeClassStructures
                            );
                            behaviorStructures.add(behaviorStructure);
                        }
                    };
                }
            });
            return behaviorStructures;
        }
    };

    @Override
    public List<BehaviorStructure> getBehaviorStructures() {
        return behaviorStructuresLazyGet.get();
    }


    @Override
    public Access getAccess() {
        return access;
    }

    @Override
    public String toString() {
        return "ClassStructureImplByAsm{" +
                "javaClassName='" + getJavaClassName() + '\'' +
                '}';
    }
}
