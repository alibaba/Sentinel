package com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructure;
import org.apache.commons.lang3.ArrayUtils;

public class ClassStructureCollectionAsserter extends MappingAsserter<String, ClassStructure> {

    public ClassStructureCollectionAsserter(Mode mode) {
        super(mode);
    }

    @Override
    protected String takeKey(ClassStructure target) {
        return target.getJavaClassName();
    }

    public static ClassStructureCollectionAsserter buildJavaClassNameArrayAsserter(final String... javaClassNameArray) {
        final ClassStructureCollectionAsserter classStructureCollectionAsserter = new ClassStructureCollectionAsserter(Mode.FULL);
        if (ArrayUtils.isNotEmpty(javaClassNameArray)) {
            for (final String javaClassName : javaClassNameArray) {
                classStructureCollectionAsserter.assertTargetByKey(
                        javaClassName,
                        new ClassStructureAsserter().assertJavaClassNameEquals(javaClassName)
                );
            }
        }
        return classStructureCollectionAsserter;
    }

    public static ClassStructureCollectionAsserter buildEmptyJavaClassNameArrayAsserter() {
        return buildJavaClassNameArrayAsserter();
    }

}
