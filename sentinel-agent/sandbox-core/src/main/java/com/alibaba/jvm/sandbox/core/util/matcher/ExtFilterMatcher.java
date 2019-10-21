package com.alibaba.jvm.sandbox.core.util.matcher;

import com.alibaba.jvm.sandbox.api.filter.AccessFlags;
import com.alibaba.jvm.sandbox.api.filter.ExtFilter;
import com.alibaba.jvm.sandbox.api.filter.ExtFilter.ExtFilterFactory;
import com.alibaba.jvm.sandbox.api.filter.Filter;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchCondition;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.Access;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.BehaviorStructure;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructure;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.alibaba.jvm.sandbox.api.filter.AccessFlags.*;

/**
 * 过滤器实现的匹配器
 *
 * @author luanjia@taobao.com
 */
public class ExtFilterMatcher implements Matcher {

    private final ExtFilter extFilter;

    public ExtFilterMatcher(final ExtFilter extFilter) {
        this.extFilter = extFilter;
    }

    // 获取需要匹配的类结构
    // 如果要匹配子类就需要将这个类的所有家族成员找出
    private Collection<ClassStructure> getWaitingMatchClassStructures(final ClassStructure classStructure) {
        final Collection<ClassStructure> waitingMatchClassStructures = new ArrayList<ClassStructure>();
        waitingMatchClassStructures.add(classStructure);
        if (extFilter.isIncludeSubClasses()) {
            waitingMatchClassStructures.addAll(classStructure.getFamilyTypeClassStructures());
        }
        return waitingMatchClassStructures;
    }

    private String[] toJavaClassNameArray(final Collection<ClassStructure> classStructures) {
        if (null == classStructures) {
            return null;
        }
        final List<String> javaClassNames = new ArrayList<String>();
        for (final ClassStructure classStructure : classStructures) {
            javaClassNames.add(classStructure.getJavaClassName());
        }
        return javaClassNames.toArray(new String[0]);
    }

    private boolean matchingClassStructure(ClassStructure classStructure) {
        for (final ClassStructure wmCs : getWaitingMatchClassStructures(classStructure)) {

            // 匹配类结构
            if (extFilter.doClassFilter(
                    toFilterAccess(wmCs.getAccess()),
                    wmCs.getJavaClassName(),
                    null == wmCs.getSuperClassStructure()
                            ? null
                            : wmCs.getSuperClassStructure().getJavaClassName(),
                    toJavaClassNameArray(wmCs.getFamilyInterfaceClassStructures()),
                    toJavaClassNameArray(wmCs.getFamilyAnnotationTypeClassStructures())
            )) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MatchingResult matching(final ClassStructure classStructure) {
        final MatchingResult result = new MatchingResult();

        // 1. 匹配ClassStructure
        if (!matchingClassStructure(classStructure)) {
            return result;
        }

        // 如果不开启加载Bootstrap的类，遇到就过滤掉
        if (!extFilter.isIncludeBootstrap()
                && classStructure.getClassLoader() == null) {
            return result;
        }

        // 2. 匹配BehaviorStructure
        for (final BehaviorStructure behaviorStructure : classStructure.getBehaviorStructures()) {
            if (extFilter.doMethodFilter(
                    toFilterAccess(behaviorStructure.getAccess()),
                    behaviorStructure.getName(),
                    toJavaClassNameArray(behaviorStructure.getParameterTypeClassStructures()),
                    toJavaClassNameArray(behaviorStructure.getExceptionTypeClassStructures()),
                    toJavaClassNameArray(behaviorStructure.getAnnotationTypeClassStructures())
            )) {
                result.getBehaviorStructures().add(behaviorStructure);
            }
        }

        return result;
    }

    /**
     * 转换为{@link AccessFlags}的Access体系
     *
     * @param access access flag
     * @return 部分兼容ASM的access flag
     */
    private static int toFilterAccess(final Access access) {
        int flag = 0;
        if (access.isPublic()) flag |= ACF_PUBLIC;
        if (access.isPrivate()) flag |= ACF_PRIVATE;
        if (access.isProtected()) flag |= ACF_PROTECTED;
        if (access.isStatic()) flag |= ACF_STATIC;
        if (access.isFinal()) flag |= ACF_FINAL;
        if (access.isInterface()) flag |= ACF_INTERFACE;
        if (access.isNative()) flag |= ACF_NATIVE;
        if (access.isAbstract()) flag |= ACF_ABSTRACT;
        if (access.isEnum()) flag |= ACF_ENUM;
        if (access.isAnnotation()) flag |= ACF_ANNOTATION;
        return flag;
    }

    /**
     * 兼容{@code sandbox-api:1.0.10}时
     * 在{@link EventWatchCondition#getOrFilterArray()}中将{@link Filter}直接暴露出来的问题，
     * 所以这里做一个兼容性的强制转换
     *
     * <ul>
     * <li>如果filterArray[index]是一个{@link ExtFilter}，则不需要再次转换</li>
     * <li>如果filterArray[index]是一个{@link Filter}，则需要进行{@link ExtFilterFactory#make(Filter)}的转换</li>
     * </ul>
     *
     * @param filterArray 过滤器数组
     * @return 兼容的Matcher
     */
    public static Matcher toOrGroupMatcher(final Filter[] filterArray) {
        final ExtFilter[] extFilterArray = new ExtFilter[filterArray.length];
        for (int index = 0; index < filterArray.length; index++) {
            extFilterArray[index] = ExtFilterFactory.make(filterArray[index]);
        }
        return toOrGroupMatcher(extFilterArray);
    }

    /**
     * 将{@link ExtFilter}数组转换为Or关系的Matcher
     *
     * @param extFilterArray 增强过滤器数组
     * @return Or关系Matcher
     */
    public static Matcher toOrGroupMatcher(final ExtFilter[] extFilterArray) {
        final Matcher[] matcherArray = new Matcher[ArrayUtils.getLength(extFilterArray)];
        for (int index = 0; index < matcherArray.length; index++) {
            matcherArray[index] = new ExtFilterMatcher(extFilterArray[index]);
        }
        return new GroupMatcher.Or(matcherArray);
    }

}
