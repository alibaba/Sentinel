package com.alibaba.jvm.sandbox.api.filter;

import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;

/**
 * 分组匹配匹配
 * 当多个过滤器需要以OR关系进行综合判断时，可以使用此类来帮助完成
 *
 * @author luanjia@taobao.com
 * @deprecated 请改用 {@link EventWatchBuilder} 来完成Filter的构建
 */
@Deprecated
public final class OrGroupFilter implements Filter {

    private final Filter[] filterArray;

    private int _access;
    private String _javaClassName;
    private String _superClassTypeJavaClassName;
    private String[] _interfaceTypeJavaClassNameArray;
    private String[] _annotationTypeJavaClassNameArray;

    /**
     * 构造组过滤器
     *
     * @param filterArray 需要进组进行判断的过滤器数组
     *                    所有过滤器的关系都是OR关系
     */
    public OrGroupFilter(final Filter... filterArray) {
        if (null == filterArray) {
            this.filterArray = new Filter[0];
        } else {
            this.filterArray = filterArray;
        }
    }


    @Override
    public boolean doClassFilter(final int access,
                                 final String javaClassName,
                                 final String superClassTypeJavaClassName,
                                 final String[] interfaceTypeJavaClassNameArray,
                                 final String[] annotationTypeJavaClassNameArray) {
        for (final Filter filter : filterArray) {
            if (filter.doClassFilter(access, javaClassName, superClassTypeJavaClassName, interfaceTypeJavaClassNameArray, annotationTypeJavaClassNameArray)) {
                this._access = access;
                this._javaClassName = javaClassName;
                this._superClassTypeJavaClassName = superClassTypeJavaClassName;
                this._interfaceTypeJavaClassNameArray = interfaceTypeJavaClassNameArray;
                this._annotationTypeJavaClassNameArray = annotationTypeJavaClassNameArray;
                return true;
            }
        }
        return false;
    }

    private boolean _doClassFilter(final Filter filter) {
        return filter.doClassFilter(
                this._access,
                this._javaClassName,
                this._superClassTypeJavaClassName,
                this._interfaceTypeJavaClassNameArray,
                this._annotationTypeJavaClassNameArray
        );
    }

    @Override
    public boolean doMethodFilter(final int access,
                                  final String javaMethodName,
                                  final String[] parameterTypeJavaClassNameArray,
                                  final String[] throwsTypeJavaClassNameArray,
                                  final String[] annotationTypeJavaClassNameArray) {
        for (final Filter filter : filterArray) {
            if (_doClassFilter(filter)
                    && filter.doMethodFilter(access, javaMethodName, parameterTypeJavaClassNameArray, throwsTypeJavaClassNameArray, annotationTypeJavaClassNameArray)) {
                return true;
            }
        }
        return false;
    }

}
