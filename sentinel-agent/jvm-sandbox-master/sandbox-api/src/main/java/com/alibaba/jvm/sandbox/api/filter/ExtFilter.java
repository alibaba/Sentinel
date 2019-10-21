package com.alibaba.jvm.sandbox.api.filter;

import com.alibaba.jvm.sandbox.api.annotation.IncludeBootstrap;
import com.alibaba.jvm.sandbox.api.annotation.IncludeSubClasses;

/**
 * 增强过滤器
 * <p>
 * 原有的{@link Filter}表现形式过于单薄而且纵深扩展能力偏差，所以使用了一些Annotation进行扩展，
 * 扩展之后的结果最终会以ExtFilter的形式在容器内部运转
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.11}
 */
public interface ExtFilter extends Filter {

    /**
     * 是否搜索子类或实现类
     *
     * @return TRUE:搜索子类或实现类;FALSE:不搜索
     */
    boolean isIncludeSubClasses();

    /**
     * 是否搜索来自BootstrapClassLoader所加载的类
     *
     * @return TRUE:搜索；FALSE：不搜索；最终容器是否会对BootstrapClassLoader所加载的类进行处理，
     * 还需要参考{@code sandbox.properties#unsafe.enable=true}配合使用才能生效
     */
    boolean isIncludeBootstrap();

    /**
     * 增强过滤器工厂类
     */
    class ExtFilterFactory {

        /**
         * 生产增强过滤器
         *
         * @param filter              原生过滤器
         * @param isIncludeSubClasses 是否包含子类
         * @param isIncludeBootstrap  是否搜索BootstrapClassLoader所加载的类
         * @return 增强过滤器
         */
        public static ExtFilter make(final Filter filter,
                                     final boolean isIncludeSubClasses,
                                     final boolean isIncludeBootstrap) {
            return new ExtFilter() {

                @Override
                public boolean isIncludeSubClasses() {
                    return isIncludeSubClasses;
                }

                @Override
                public boolean isIncludeBootstrap() {
                    return isIncludeBootstrap;
                }

                @Override
                public boolean doClassFilter(final int access,
                                             final String javaClassName,
                                             final String superClassTypeJavaClassName,
                                             final String[] interfaceTypeJavaClassNameArray,
                                             final String[] annotationTypeJavaClassNameArray) {
                    return filter.doClassFilter(
                            access,
                            javaClassName,
                            superClassTypeJavaClassName,
                            interfaceTypeJavaClassNameArray,
                            annotationTypeJavaClassNameArray
                    );
                }

                @Override
                public boolean doMethodFilter(final int access,
                                              final String javaMethodName,
                                              final String[] parameterTypeJavaClassNameArray,
                                              final String[] throwsTypeJavaClassNameArray,
                                              final String[] annotationTypeJavaClassNameArray) {
                    return filter.doMethodFilter(
                            access,
                            javaMethodName,
                            parameterTypeJavaClassNameArray,
                            throwsTypeJavaClassNameArray,
                            annotationTypeJavaClassNameArray
                    );
                }
            };
        }

        /**
         * 生产增强过滤器
         *
         * @param filter 原生过滤器
         * @return 增强过滤器
         */
        public static ExtFilter make(final Filter filter) {
            return
                    filter instanceof ExtFilter
                            ? (ExtFilter) filter
                            : make(
                            filter,
                            filter.getClass().isAnnotationPresent(IncludeSubClasses.class),
                            filter.getClass().isAnnotationPresent(IncludeBootstrap.class)
                    );
        }

    }

}
