package com.alibaba.jvm.sandbox.core.util.matcher;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.BehaviorStructure;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructure;

import java.util.LinkedHashSet;

public abstract class GroupMatcher implements Matcher {

    final Matcher[] matcherArray;

    private GroupMatcher(final Matcher... matcherArray) {
        this.matcherArray = matcherArray;
    }


    public static final class Or extends GroupMatcher {

        public Or(Matcher... matcherArray) {
            super(matcherArray);
        }

        @Override
        public MatchingResult matching(final ClassStructure classStructure) {
            final MatchingResult result = new MatchingResult();
            if (null == matcherArray) {
                return result;
            }
            for (final Matcher subMatcher : matcherArray) {
                result.getBehaviorStructures().addAll(subMatcher.matching(classStructure).getBehaviorStructures());
            }
            return result;
        }

    }

    public static final class And extends GroupMatcher {

        public And(Matcher... matcherArray) {
            super(matcherArray);
        }

        @Override
        public MatchingResult matching(ClassStructure classStructure) {
            boolean isFirst = true;
            final MatchingResult result = new MatchingResult();
            final LinkedHashSet<BehaviorStructure> found = new LinkedHashSet<BehaviorStructure>();
            if (null == matcherArray) {
                return result;
            }
            for (final Matcher subMatcher : matcherArray) {
                final MatchingResult subResult = subMatcher.matching(classStructure);

                // 只要有一次匹配失败，剩下的是取交集运算，所以肯定也没戏，就不用花这个计算了
                if (!subResult.isMatched()) {
                    return result;
                }

                if (isFirst) {
                    found.addAll(subResult.getBehaviorStructures());
                    isFirst = false;
                } else {
                    found.retainAll(subResult.getBehaviorStructures());
                }
            }
            if (!found.isEmpty()) {
                result.getBehaviorStructures().addAll(found);
            }
            return result;
        }

    }

}
