package com.alibaba.jvm.sandbox.qatest.core.util.matcher;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructure;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructureFactory;
import com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.BehaviorStructureAsserter;
import com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.BehaviorStructureCollectionAsserter;
import com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.ClassStructureAsserter;
import com.alibaba.jvm.sandbox.qatest.core.util.matcher.target.ChildClass;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

import static com.alibaba.jvm.sandbox.qatest.core.util.QaClassUtils.toByteArray;
import static com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.AccessAsserter.AccessIsEnum.*;
import static com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.BehaviorStructureCollectionAsserter.buildBehaviorSignCodeArrayAsserter;
import static com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.ClassStructureCollectionAsserter.buildEmptyJavaClassNameArrayAsserter;
import static com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.ClassStructureCollectionAsserter.buildJavaClassNameArrayAsserter;
import static com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.MappingAsserter.Mode.ALL;
import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class ClassStructureByChildClassTestCase {

    private static final String PACKAGE_PREFIX = "com.alibaba.jvm.sandbox.qatest.core.util.matcher.target.";

    @Parameterized.Parameters
    public static Collection<Object[]> getData() throws IOException {
        return asList(
                new Object[]{ClassStructureFactory.createClassStructure(ChildClass.class)},
                new Object[]{ClassStructureFactory.createClassStructure(toByteArray(ChildClass.class), ChildClass.class.getClassLoader())}
        );
    }

    private final ClassStructure childClassStructure;

    public ClassStructureByChildClassTestCase(ClassStructure childClassStructure) {
        this.childClassStructure = childClassStructure;
    }


    private static String buildSignCode(final String javaClassName,
                                        final String name,
                                        final String... parameterJavaClassName) {
        return String.format(
                "%s#%s(%s)",
                javaClassName,
                name,
                StringUtils.join(parameterJavaClassName, ",")
        );
    }

    @Test
    public void test$$ChildClassStructure() {

        new ClassStructureAsserter()
                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass")
                .assertAccess(IS_PUBLIC)
                .assertSuper(
                        new ClassStructureAsserter()
                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ParentClass")
                                .assertAccess(IS_PUBLIC, IS_ABSTRACT)
                )
                .assertInterfaces(buildEmptyJavaClassNameArrayAsserter())
                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                .assertFamilySupers(
                        buildJavaClassNameArrayAsserter(
                                PACKAGE_PREFIX + "ParentClass",
                                PACKAGE_PREFIX + "GrandpaClass"
                        )
                )
                .assertFamilyInterfaces(
                        buildJavaClassNameArrayAsserter(
                                PACKAGE_PREFIX + "IParentInterfaceFirst",
                                PACKAGE_PREFIX + "IParentInterfaceFirstFirst",
                                PACKAGE_PREFIX + "IParentInterfaceFirstSecond",
                                PACKAGE_PREFIX + "IParentInterfaceSecond",
                                PACKAGE_PREFIX + "IGrandpaInterfaceFirst",
                                PACKAGE_PREFIX + "IGrandpaInterfaceFirstFirst"
                        )
                )
                .assertFamilyTypes(
                        buildJavaClassNameArrayAsserter(
                                PACKAGE_PREFIX + "ParentClass",
                                PACKAGE_PREFIX + "GrandpaClass",
                                PACKAGE_PREFIX + "IParentInterfaceFirst",
                                PACKAGE_PREFIX + "IParentInterfaceFirstFirst",
                                PACKAGE_PREFIX + "IParentInterfaceFirstSecond",
                                PACKAGE_PREFIX + "IParentInterfaceSecond",
                                PACKAGE_PREFIX + "IGrandpaInterfaceFirst",
                                PACKAGE_PREFIX + "IGrandpaInterfaceFirstFirst"
                        )
                )
                .assertFamilyAnnotationTypes(
                        buildJavaClassNameArrayAsserter(
                                PACKAGE_PREFIX + "InheritedAnnotation"
                        )
                )
                .assertBehaviors(
                        buildBehaviorSignCodeArrayAsserter(
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "<init>"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfSumIntArray", "int[]"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnPublicInterface"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnProtectedInterface"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnPublicStaticClass"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnProtectedStaticClass"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnPublicEnum"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnInnerClass"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnProtectedEnum"),
                                buildSignCode(
                                        PACKAGE_PREFIX + "ChildClass",
                                        "methodOfSingleArguments",
                                        PACKAGE_PREFIX + "ChildClass$PublicInterface",
                                        PACKAGE_PREFIX + "ChildClass$ProtectedInterface",
                                        PACKAGE_PREFIX + "ChildClass$PublicStaticClass",
                                        PACKAGE_PREFIX + "ChildClass$ProtectedStaticClass",
                                        PACKAGE_PREFIX + "ChildClass$InnerClass",
                                        PACKAGE_PREFIX + "ChildClass$PublicEnum",
                                        PACKAGE_PREFIX + "ChildClass$ProtectedEnum"
                                ),
                                buildSignCode(
                                        PACKAGE_PREFIX + "ChildClass",
                                        "methodOfArrayArguments",
                                        PACKAGE_PREFIX + "ChildClass$PublicInterface[]",
                                        PACKAGE_PREFIX + "ChildClass$ProtectedInterface[]",
                                        PACKAGE_PREFIX + "ChildClass$PublicStaticClass[]",
                                        PACKAGE_PREFIX + "ChildClass$ProtectedStaticClass[]",
                                        PACKAGE_PREFIX + "ChildClass$InnerClass[]",
                                        PACKAGE_PREFIX + "ChildClass$PublicEnum[]",
                                        PACKAGE_PREFIX + "ChildClass$ProtectedEnum[]"
                                ),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfChildClassWithAnnotation"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfPrivateStatic"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfPrivateNative"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfParentIsAbstract"),
                                buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfParentInterfaceFirstFirstWithAnnotation")
                        )
                )
                .assertThat("ChildClass", childClassStructure);
    }

    @Test
    public void test$$ChildClassStructure$$methodOfReturn() {
        new ClassStructureAsserter()
                .assertBehaviors(
                        new BehaviorStructureCollectionAsserter(ALL)
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfSumIntArray", "int[]"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(new ClassStructureAsserter().assertJavaClassNameEquals("void"))
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnPublicInterface"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(
                                                        new ClassStructureAsserter()
                                                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass$PublicInterface")
                                                                .assertAccess(IS_PUBLIC, IS_INTERFACE, IS_STATIC, IS_ABSTRACT)
                                                )
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnProtectedInterface"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(
                                                        new ClassStructureAsserter()
                                                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass$ProtectedInterface")
                                                                .assertAccess(IS_PROTECTED, IS_INTERFACE, IS_STATIC, IS_ABSTRACT)
                                                )
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnPublicStaticClass"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(
                                                        new ClassStructureAsserter()
                                                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass$PublicStaticClass")
                                                                .assertAccess(IS_PUBLIC, IS_STATIC)
                                                )
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnProtectedStaticClass"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(
                                                        new ClassStructureAsserter()
                                                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass$ProtectedStaticClass")
                                                                .assertAccess(IS_PROTECTED, IS_STATIC)
                                                )
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnPublicEnum"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(
                                                        new ClassStructureAsserter()
                                                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass$PublicEnum")
                                                                .assertAccess(IS_PUBLIC, IS_FINAL, IS_ENUM, IS_STATIC)
                                                )
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnInnerClass"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(
                                                        new ClassStructureAsserter()
                                                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass$InnerClass")
                                                                .assertAccess()
                                                )
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfReturnProtectedEnum"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(
                                                        new ClassStructureAsserter()
                                                                .assertJavaClassNameEquals(PACKAGE_PREFIX + "ChildClass$ProtectedEnum")
                                                                .assertAccess(IS_PROTECTED, IS_FINAL, IS_ENUM, IS_STATIC)
                                                )
                                                .assertAccess(IS_PUBLIC)
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertExceptionTypes(buildEmptyJavaClassNameArrayAsserter())
                                )
                )
                .assertThat("ChildClass", childClassStructure);
    }

    @Test
    public void test$$ChildClassStructure$$methodOfSingleArguments() {
        new ClassStructureAsserter()
                .assertBehaviors(
                        new BehaviorStructureCollectionAsserter(ALL)
                                .assertTargetByKey(
                                        buildSignCode(
                                                PACKAGE_PREFIX + "ChildClass",
                                                "methodOfSingleArguments",
                                                PACKAGE_PREFIX + "ChildClass$PublicInterface",
                                                PACKAGE_PREFIX + "ChildClass$ProtectedInterface",
                                                PACKAGE_PREFIX + "ChildClass$PublicStaticClass",
                                                PACKAGE_PREFIX + "ChildClass$ProtectedStaticClass",
                                                PACKAGE_PREFIX + "ChildClass$InnerClass",
                                                PACKAGE_PREFIX + "ChildClass$PublicEnum",
                                                PACKAGE_PREFIX + "ChildClass$ProtectedEnum"
                                        ),
                                        new BehaviorStructureAsserter()
                                                .assertAccess(IS_PUBLIC)
                                )
                )
                .assertThat("ChildClass", childClassStructure);
    }

    @Test
    public void test$$ChildClassStructure$$methodOfArrayArguments() {
        new ClassStructureAsserter()
                .assertBehaviors(
                        new BehaviorStructureCollectionAsserter(ALL)
                                .assertTargetByKey(
                                        buildSignCode(
                                                PACKAGE_PREFIX + "ChildClass",
                                                "methodOfArrayArguments",
                                                PACKAGE_PREFIX + "ChildClass$PublicInterface[]",
                                                PACKAGE_PREFIX + "ChildClass$ProtectedInterface[]",
                                                PACKAGE_PREFIX + "ChildClass$PublicStaticClass[]",
                                                PACKAGE_PREFIX + "ChildClass$ProtectedStaticClass[]",
                                                PACKAGE_PREFIX + "ChildClass$InnerClass[]",
                                                PACKAGE_PREFIX + "ChildClass$PublicEnum[]",
                                                PACKAGE_PREFIX + "ChildClass$ProtectedEnum[]"
                                        ),
                                        new BehaviorStructureAsserter()
                                                .assertAccess(IS_PUBLIC)
                                )
                )
                .assertThat("ChildClass", childClassStructure);
    }

    @Test
    public void test$$ChildClassStructure$$methodOfChildClassWithAnnotation() {
        new ClassStructureAsserter()
                .assertBehaviors(
                        new BehaviorStructureCollectionAsserter(ALL)
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfChildClassWithAnnotation"),
                                        new BehaviorStructureAsserter()
                                                .assertExceptionTypes(buildJavaClassNameArrayAsserter(
                                                        "java.lang.IllegalStateException",
                                                        "java.lang.RuntimeException"
                                                ))
                                                .assertAnnotationTypes(buildJavaClassNameArrayAsserter(
                                                        PACKAGE_PREFIX + "InheritedAnnotation"
                                                ))
                                                .assertAccess(IS_PUBLIC)
                                )
                )
                .assertThat("ChildClass", childClassStructure);
    }

    @Test
    public void test$$ChildClassStructure$$methodOfPrivateStatic() {
        new ClassStructureAsserter()
                .assertBehaviors(
                        new BehaviorStructureCollectionAsserter(ALL)
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfPrivateStatic"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(new ClassStructureAsserter().assertJavaClassNameEquals("void"))
                                                .assertAccess(IS_PRIVATE, IS_STATIC)
                                )
                )
                .assertThat("ChildClass", childClassStructure);
    }

    @Test
    public void test$$ChildClassStructure$$methodOfPrivateNative() {
        new ClassStructureAsserter()
                .assertBehaviors(
                        new BehaviorStructureCollectionAsserter(ALL)
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfPrivateNative"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(new ClassStructureAsserter().assertJavaClassNameEquals("void"))
                                                .assertAccess(IS_PRIVATE, IS_NATIVE)
                                )
                )
                .assertThat("ChildClass", childClassStructure);
    }

    @Test
    public void test$$ChildClassStructure$$methodOfParentInterfaceFirstFirstWithAnnotation() {
        new ClassStructureAsserter()
                .assertBehaviors(
                        new BehaviorStructureCollectionAsserter(ALL)
                                .assertTargetByKey(
                                        buildSignCode(PACKAGE_PREFIX + "ChildClass", "methodOfParentInterfaceFirstFirstWithAnnotation"),
                                        new BehaviorStructureAsserter()
                                                .assertReturnType(new ClassStructureAsserter().assertJavaClassNameEquals("void"))
                                                .assertAnnotationTypes(buildEmptyJavaClassNameArrayAsserter())
                                                .assertAccess(IS_PUBLIC)
                                )
                )
                .assertThat("ChildClass", childClassStructure);
    }

}
