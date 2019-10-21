package com.alibaba.jvm.sandbox.core.util.matcher;

import com.alibaba.jvm.sandbox.api.annotation.Stealth;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.Access;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.BehaviorStructure;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructure;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructureFactory.createClassStructure;

/**
 * 不支持的类匹配
 *
 * @author luanjia@taobao.com
 */
public class UnsupportedMatcher implements Matcher {

    private final ClassLoader loader;
    private final boolean isEnableUnsafe;

    public UnsupportedMatcher(final ClassLoader loader,
                              final boolean isEnableUnsafe) {
        this.loader = loader;
        this.isEnableUnsafe = isEnableUnsafe;
    }

    /*
     * 是否因sandbox容器本身缺陷所暂时无法支持的类
     */
    private boolean isUnsupportedClass(final ClassStructure classStructure) {
        return StringUtils.containsAny(
                classStructure.getJavaClassName(),
                "$$Lambda$",
                "$$FastClassBySpringCGLIB$$",
                "$$EnhancerBySpringCGLIB$$",
                "$$EnhancerByCGLIB$$",
                "$$FastClassByCGLIB$$"
        );
    }

    /*
     * 是否是sandbox容器本身的类
     * 因为多命名空间的原因，所以这里不能简单的用ClassLoader来进行判断
     */
    private boolean isJvmSandboxClass(final ClassStructure classStructure) {
        return classStructure.getJavaClassName().startsWith("com.alibaba.jvm.sandbox.");
    }

    private Set<String> takeJavaClassNames(final Set<ClassStructure> classStructures) {
        final Set<String> javaClassNames = new LinkedHashSet<String>();
        for (final ClassStructure classStructure : classStructures) {
            javaClassNames.add(classStructure.getJavaClassName());
        }
        return javaClassNames;
    }

    /*
     * 判断是否隐形类
     */
    private boolean isStealthClass(final ClassStructure classStructure) {
        return takeJavaClassNames(classStructure.getFamilyAnnotationTypeClassStructures())
                .contains(Stealth.class.getName());
    }

    /*
     * 判断是否ClassLoader家族中是否有隐形基因
     */
    private boolean isFromStealthClassLoader() {
        if (null == loader) {
            return !isEnableUnsafe;
        }
        return takeJavaClassNames(createClassStructure(loader.getClass()).getFamilyTypeClassStructures())
                .contains(Stealth.class.getName());
    }

    /*
     * 是否是负责启动的main函数
     * 这个函数如果被增强了会引起错误,所以千万不能增强,嗯嗯
     * public static void main(String[]);
     */
    private boolean isJavaMainBehavior(final BehaviorStructure behaviorStructure) {
        final Access access = behaviorStructure.getAccess();
        final List<ClassStructure> parameterTypeClassStructures = behaviorStructure.getParameterTypeClassStructures();
        return access.isPublic()
                && access.isStatic()
                && "void".equals(behaviorStructure.getReturnTypeClassStructure().getJavaClassName())
                && "main".equals(behaviorStructure.getName())
                && parameterTypeClassStructures.size() == 1
                && "java.lang.String[]".equals(parameterTypeClassStructures.get(0).getJavaClassName());
    }

    /*
     * 是否不支持的方法修饰
     * 1. abstract的方法没有实现，没有必要增强
     * 2. native的方法暂时无法支持
     */
    private boolean isUnsupportedBehavior(final BehaviorStructure behaviorStructure) {
        final Access access = behaviorStructure.getAccess();
        return access.isAbstract()
                || access.isNative();
    }

    @Override
    public MatchingResult matching(final ClassStructure classStructure) {
        final MatchingResult result = new MatchingResult();
        if (isUnsupportedClass(classStructure)
                || isJvmSandboxClass(classStructure)
                || isFromStealthClassLoader()
                || isStealthClass(classStructure)) {
            return result;
        }
        for (final BehaviorStructure behaviorStructure : classStructure.getBehaviorStructures()) {
            if (isJavaMainBehavior(behaviorStructure)
                    || isUnsupportedBehavior(behaviorStructure)) {
                continue;
            }
            result.getBehaviorStructures().add(behaviorStructure);
        }
        return result;
    }


    /**
     * 构造AND关系的组匹配
     * <p>
     * 一般{@link UnsupportedMatcher}都与其他Matcher配合使用，
     * 所以这里对AND关系做了一层封装
     * </p>
     *
     * @param matcher 发生AND关系的{@link Matcher}
     * @return GroupMatcher.and(matcher, this)
     */
    public Matcher and(final Matcher matcher) {
        return new GroupMatcher.And(
                matcher,
                this
        );
    }

}
