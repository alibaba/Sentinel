package com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.BehaviorStructure;
import org.apache.commons.lang3.ArrayUtils;

public class BehaviorStructureCollectionAsserter extends MappingAsserter<String, BehaviorStructure> {

    public BehaviorStructureCollectionAsserter(Mode mode) {
        super(mode);
    }

    @Override
    protected String takeKey(BehaviorStructure target) {
        return target.getSignCode();
    }

    @Override
    public BehaviorStructureCollectionAsserter assertTargetByKey(String key, Asserter<BehaviorStructure> targetAsserter) {
        super.assertTargetByKey(key, targetAsserter);
        return this;
    }

    public static BehaviorStructureCollectionAsserter buildBehaviorSignCodeArrayAsserter(final String... behaviorSignCodeArray) {
        final BehaviorStructureCollectionAsserter behaviorStructureCollectionAsserter = new BehaviorStructureCollectionAsserter(Mode.FULL);
        if (ArrayUtils.isNotEmpty(behaviorSignCodeArray)) {
            for (final String behaviorSignCode : behaviorSignCodeArray) {
                behaviorStructureCollectionAsserter.assertTargetByKey(
                        behaviorSignCode,
                        new BehaviorStructureAsserter().assertSignCodeEquals(behaviorSignCode)
                );
            }
        }
        return behaviorStructureCollectionAsserter;
    }

}
