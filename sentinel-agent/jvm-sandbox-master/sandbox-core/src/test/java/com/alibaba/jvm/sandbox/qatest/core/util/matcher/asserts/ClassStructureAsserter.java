package com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructure;
import com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.AccessAsserter.AccessIsEnum;

import static org.junit.Assert.assertEquals;

public class ClassStructureAsserter implements Asserter<ClassStructure> {

    private AccessAsserter accessAsserter;
    private String expectedJavaClassName;

    private ClassStructureAsserter superClassStructureAsserter;
    private ClassStructureCollectionAsserter interfaceClassStructureCollectionAsserter;
    private ClassStructureCollectionAsserter annotationTypeClassStructureCollectionAsserter;

    private ClassStructureCollectionAsserter familySuperClassStructureCollectionAsserter;
    private ClassStructureCollectionAsserter familyInterfaceClassStructureCollectionAsserter;
    private ClassStructureCollectionAsserter familyTypeClassStructureCollectionAsserter;
    private ClassStructureCollectionAsserter familyAnnotationTypeClassStructureCollectionAsserter;

    private BehaviorStructureCollectionAsserter behaviorStructureCollectionAsserter;


    public ClassStructureAsserter assertAccess(final AccessIsEnum... accessIsEnumArray) {
        this.accessAsserter = new AccessAsserter(accessIsEnumArray);
        return this;
    }

    public ClassStructureAsserter assertJavaClassNameEquals(final String expectedJavaClassName) {
        this.expectedJavaClassName = expectedJavaClassName;
        return this;
    }

    public ClassStructureAsserter assertSuper(ClassStructureAsserter superClassStructureAsserter) {
        this.superClassStructureAsserter = superClassStructureAsserter;
        return this;
    }

    public ClassStructureAsserter assertInterfaces(ClassStructureCollectionAsserter interfaceClassStructureCollectionAsserter) {
        this.interfaceClassStructureCollectionAsserter = interfaceClassStructureCollectionAsserter;
        return this;
    }

    public ClassStructureAsserter assertAnnotationTypes(ClassStructureCollectionAsserter annotationTypeClassStructureCollectionAsserter) {
        this.annotationTypeClassStructureCollectionAsserter = annotationTypeClassStructureCollectionAsserter;
        return this;
    }

    public ClassStructureAsserter assertFamilySupers(ClassStructureCollectionAsserter familySuperClassStructureCollectionAsserter) {
        this.familySuperClassStructureCollectionAsserter = familySuperClassStructureCollectionAsserter;
        return this;
    }

    public ClassStructureAsserter assertFamilyInterfaces(ClassStructureCollectionAsserter familyInterfaceClassStructureCollectionAsserter) {
        this.familyInterfaceClassStructureCollectionAsserter = familyInterfaceClassStructureCollectionAsserter;
        return this;
    }

    public ClassStructureAsserter assertFamilyTypes(ClassStructureCollectionAsserter familyTypeClassStructureCollectionAsserter) {
        this.familyTypeClassStructureCollectionAsserter = familyTypeClassStructureCollectionAsserter;
        return this;
    }

    public ClassStructureAsserter assertFamilyAnnotationTypes(ClassStructureCollectionAsserter familyAnnotationTypeClassStructureCollectionAsserter) {
        this.familyAnnotationTypeClassStructureCollectionAsserter = familyAnnotationTypeClassStructureCollectionAsserter;
        return this;
    }

    public ClassStructureAsserter assertBehaviors(BehaviorStructureCollectionAsserter behaviorStructureCollectionAsserter) {
        this.behaviorStructureCollectionAsserter = behaviorStructureCollectionAsserter;
        return this;
    }

    @Override
    public void assertThat(final String message,
                           final ClassStructure classStructure) {

        final String prefix = message + ":@[" + classStructure.getJavaClassName() + "]";

        if (null != accessAsserter) {
            accessAsserter.assertThat(prefix + ":Access", classStructure.getAccess());
        }

        if (null != expectedJavaClassName) {
            assertEquals(prefix + ":JavaClassName", expectedJavaClassName, classStructure.getJavaClassName());
        }

        if (null != superClassStructureAsserter) {
            superClassStructureAsserter.assertThat(prefix + ":SuperClass", classStructure.getSuperClassStructure());
        }

        if (null != interfaceClassStructureCollectionAsserter) {
            interfaceClassStructureCollectionAsserter
                    .assertThat(prefix + ":Interfaces", classStructure.getInterfaceClassStructures());
        }

        if (null != annotationTypeClassStructureCollectionAsserter) {
            annotationTypeClassStructureCollectionAsserter
                    .assertThat(prefix + ":AnnotationTypes", classStructure.getAnnotationTypeClassStructures());
        }


        if (null != familySuperClassStructureCollectionAsserter) {
            familySuperClassStructureCollectionAsserter
                    .assertThat(prefix + ":FamilySuperClasses", classStructure.getFamilySuperClassStructures());
        }

        if (null != familyInterfaceClassStructureCollectionAsserter) {
            familyInterfaceClassStructureCollectionAsserter
                    .assertThat(prefix + ":FamilyInterfaces", classStructure.getFamilyInterfaceClassStructures());
        }

        if (null != familyTypeClassStructureCollectionAsserter) {
            familyTypeClassStructureCollectionAsserter
                    .assertThat(prefix + ":FamilyTypes", classStructure.getFamilyTypeClassStructures());
        }

        if (null != familyAnnotationTypeClassStructureCollectionAsserter) {
            familyAnnotationTypeClassStructureCollectionAsserter
                    .assertThat(prefix + ":FamilyAnnotationTypes", classStructure.getFamilyAnnotationTypeClassStructures());
        }

        if (null != behaviorStructureCollectionAsserter) {
            behaviorStructureCollectionAsserter
                    .assertThat(prefix + ":Behaviors", classStructure.getBehaviorStructures());
        }

    }

}
