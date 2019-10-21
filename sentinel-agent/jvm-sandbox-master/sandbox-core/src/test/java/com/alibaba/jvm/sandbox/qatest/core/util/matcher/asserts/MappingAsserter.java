package com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts;

import java.util.*;

import static com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.MappingAsserter.Mode.FULL;
import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class MappingAsserter<K, T> implements Asserter<Collection<T>> {

    /**
     * 匹配模式
     */
    public enum Mode {

        /**
         * 对添加的{@code <T>}必须全部断言通过，不允许有遗漏
         * 目标{@code <K>}允许出现不在断言判断集合中的类
         */
        ALL,

        /**
         * 对添加的{@code <T>}必须全部断言通过，不允许有遗漏
         * 目标{@code <K>}不允许出现不在断言判断集合中的类
         */
        FULL
    }


    private final Mode mode;
    private final Map<K, Asserter<T>> mappingOfAsserter = new HashMap<K, Asserter<T>>();

    public MappingAsserter(Mode mode) {
        this.mode = mode;
    }

    public MappingAsserter<K, T> assertTargetByKey(final K key,
                                                   final Asserter<T> targetAsserter) {
        mappingOfAsserter.put(key, targetAsserter);
        return this;
    }

    abstract protected K takeKey(T target);

    @Override
    public void assertThat(String message, Collection<T> targets) {
        final Set<K> targetKeySet = new LinkedHashSet<K>();
        for (final T target : targets) {
            final K targetKey = takeKey(target);
            if (!mappingOfAsserter.containsKey(targetKey)) {
                assertFalse(
                        message + String.format(
                                ":\"%s\" not contains in \n[\n\t%s\n]",
                                targetKey,
                                join(mappingOfAsserter.keySet().toArray(new String[]{}), ",\n\t")
                        ),
                        mode == FULL
                );
                continue;
            }
            targetKeySet.add(targetKey);
            mappingOfAsserter
                    .get(targetKey)
                    .assertThat(message, target);
        }

        // 不允许有遗漏匹配，那么所有Key都必须在其中
        for (final K expectedKey : mappingOfAsserter.keySet()) {
            assertTrue(
                    message + String.format(
                            ":\"%s\" is missing in \n[\n\t%s\n]",
                            expectedKey,
                            join(targetKeySet.toArray(new String[]{}), ",\n\t")
                    ),
                    targetKeySet.contains(expectedKey)
            );
        }
    }

}
