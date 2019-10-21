package com.alibaba.jvm.sandbox.api.filter;

/**
 * 类名和方法名正则表达式匹配过滤器
 *
 * @author luanjia@taobao.com
 */
public class NameRegexFilter implements Filter {

    // 类名正则表达式
    private final String javaNameRegex;

    // 方法名正则表达式
    private final String javaMethodRegex;

    /**
     * 构造名称正则表达式过滤器
     *
     * @param javaNameRegex   类名正则表达式
     * @param javaMethodRegex 方法名正则表达式
     */
    public NameRegexFilter(String javaNameRegex, String javaMethodRegex) {
        this.javaNameRegex = javaNameRegex;
        this.javaMethodRegex = javaMethodRegex;
    }

    @Override
    public boolean doClassFilter(final int access,
                                 final String javaClassName,
                                 final String superClassTypeJavaClassName,
                                 final String[] interfaceTypeJavaClassNameArray,
                                 final String[] annotationTypeJavaClassNameArray) {
        return javaClassName.matches(javaNameRegex);
    }

    @Override
    public boolean doMethodFilter(final int access,
                                  final String javaMethodName,
                                  final String[] parameterTypeJavaClassNameArray,
                                  final String[] throwsTypeJavaClassNameArray,
                                  final String[] annotationTypeJavaClassNameArray) {
        return javaMethodName.matches(javaMethodRegex);
    }

}
