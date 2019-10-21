package com.alibaba.jvm.sandbox.core.manager.impl;

import com.alibaba.jvm.sandbox.core.util.ObjectIDs;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 增强类的统计器
 * <p>
 * 影响类和方法的统计信息
 * </p>
 *
 * @author luanjia@taobao.com
 */
public class AffectStatistic {

    // 影响类去重码集合
    private final Set<String> affectClassUniqueSet = new HashSet<String>();

    // 影响方法去重码集合
    private final Set<String> affectMethodUniqueSet = new HashSet<String>();

    // 计算唯一编码
    private String computeUniqueCode(final ClassLoader loader, final String javaClassName) {
        return new StringBuilder()
                .append(ObjectIDs.instance.identity(loader))
                .append("_c_")
                .append(javaClassName)
                .toString();
    }

    private Set<String> computeUniqueCode(final ClassLoader loader,
                                          final Set<String> behaviorSignCodes) {
        final Set<String> uniqueCodes = new LinkedHashSet<String>();
        for (final String behaviorSignCode : behaviorSignCodes) {
            uniqueCodes.add(
                    ObjectIDs.instance.identity(loader)
                            + "_h_"
                            + behaviorSignCode
            );
        }
        return uniqueCodes;
    }

    /**
     * 统计影响的类个数
     *
     * @param loader        加载的ClassLoader
     * @param javaClassName 类名
     */
    public void statisticAffectClass(final ClassLoader loader,
                                     final String javaClassName) {
        affectClassUniqueSet.add(computeUniqueCode(loader, javaClassName));
    }

    /**
     * 统计影响的行为个数
     *
     * @param loader            加载的ClassLoader
     * @param behaviorSignCodes 行为签名集合
     */
    public void statisticAffectMethod(final ClassLoader loader,
                                      final Set<String> behaviorSignCodes) {
        affectMethodUniqueSet.addAll(computeUniqueCode(loader, behaviorSignCodes));
    }

    /**
     * 对本次影响范围进行统计
     *
     * @param loader            加载的ClassLoader
     * @param internalClassName 类名
     * @param behaviorSignCodes 行为签名集合
     */
    public void statisticAffect(final ClassLoader loader,
                                final String internalClassName,
                                final Set<String> behaviorSignCodes) {
        statisticAffectClass(loader, internalClassName);
        statisticAffectMethod(loader, behaviorSignCodes);
    }


    /**
     * 获取影响类数量
     *
     * @return 影响类数量
     */
    public int cCnt() {
        return affectClassUniqueSet.size();
    }

    /**
     * 获取影响方法数量
     *
     * @return 影响方法数量
     */
    public int mCnt() {
        return affectMethodUniqueSet.size();
    }

}
