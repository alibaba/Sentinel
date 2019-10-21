package com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.BehaviorStructure;
import com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.AccessAsserter.AccessIsEnum;

import static org.junit.Assert.assertEquals;

public class BehaviorStructureAsserter implements Asserter<BehaviorStructure> {

    private AccessAsserter accessAsserter;
    private String expectedBehaviorName;
    private String expectedSignCode;
    private ClassStructureAsserter returnTypeClassStructureAsserter;
    private ClassStructureCollectionAsserter exceptionTypeClassStructureCollectionAsserter;
    private ClassStructureCollectionAsserter annotationTypeClassStructureCollectionAsserter;

    public BehaviorStructureAsserter assertAccess(final AccessIsEnum... accessIsEnumArray) {
        this.accessAsserter = new AccessAsserter(accessIsEnumArray);
        return this;
    }

    public BehaviorStructureAsserter assertBehaviorNameEquals(final String expectedBehaviorName) {
        this.expectedBehaviorName = expectedBehaviorName;
        return this;
    }

    public BehaviorStructureAsserter assertSignCodeEquals(final String expectedSignCode) {
        this.expectedSignCode = expectedSignCode;
        return this;
    }

    public BehaviorStructureAsserter assertReturnType(final ClassStructureAsserter returnTypeClassStructureAsserter) {
        this.returnTypeClassStructureAsserter = returnTypeClassStructureAsserter;
        return this;
    }

    public BehaviorStructureAsserter assertExceptionTypes(final ClassStructureCollectionAsserter exceptionTypeClassStructureCollectionAsserter) {
        this.exceptionTypeClassStructureCollectionAsserter = exceptionTypeClassStructureCollectionAsserter;
        return this;
    }

    public BehaviorStructureAsserter assertAnnotationTypes(final ClassStructureCollectionAsserter annotationTypeClassStructureCollectionAsserter) {
        this.annotationTypeClassStructureCollectionAsserter = annotationTypeClassStructureCollectionAsserter;
        return this;
    }

    @Override
    public void assertThat(final String message,
                           final BehaviorStructure behaviorStructure) {

        final String prefix = message + ":@[" + behaviorStructure.getName() + "]";

        if (null != accessAsserter) {
            accessAsserter.assertThat(prefix + ":Access", behaviorStructure.getAccess());
        }

        if (null != expectedBehaviorName) {
            assertEquals(prefix + ":Name", expectedBehaviorName, behaviorStructure.getName());
        }

        if (null != expectedSignCode) {
            assertEquals(prefix + ":SignCode", expectedSignCode, behaviorStructure.getSignCode());
        }

        if (null != returnTypeClassStructureAsserter) {
            returnTypeClassStructureAsserter
                    .assertThat(prefix + ":ReturnType", behaviorStructure.getReturnTypeClassStructure());
        }

        if (null != exceptionTypeClassStructureCollectionAsserter) {
            exceptionTypeClassStructureCollectionAsserter
                    .assertThat(prefix + ":ExceptionTypes", behaviorStructure.getExceptionTypeClassStructures());
        }

        if (null != annotationTypeClassStructureCollectionAsserter) {
            annotationTypeClassStructureCollectionAsserter
                    .assertThat(prefix + ":AnnotationTypes", behaviorStructure.getAnnotationTypeClassStructures());
        }

    }

}
