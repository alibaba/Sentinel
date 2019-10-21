package com.alibaba.jvm.sandbox.module.debug.util;

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;

/**
 * 表达式
 *
 * @author oldmanpushcart@gamil.com
 */
public interface Express {

    /**
     * 表达式异常
     * Created by oldmanpushcart@gmail.com on 15/5/20.
     */
    class ExpressException extends Exception {

        private final String express;

        /**
         * 表达式异常
         *
         * @param express 原始表达式
         * @param cause   异常原因
         */
        public ExpressException(String express, Throwable cause) {
            super(cause);
            this.express = express;
        }

        /**
         * 获取表达式
         *
         * @return 返回出问题的表达式
         */
        public String getExpress() {
            return express;
        }
    }


    /**
     * 根据表达式获取值
     *
     * @param express 表达式
     * @return 表达式运算后的值
     * @throws ExpressException 表达式运算出错
     */
    Object get(String express) throws ExpressException;

    /**
     * 根据表达式判断是与否
     *
     * @param express 表达式
     * @return 表达式运算后的布尔值
     * @throws ExpressException 表达式运算出错
     */
    boolean is(String express) throws ExpressException;

    /**
     * 绑定对象
     *
     * @param object 待绑定对象
     * @return this
     */
    Express bind(Object object);

    /**
     * 绑定变量
     *
     * @param name  变量名
     * @param value 变量值
     * @return this
     */
    Express bind(String name, Object value);

    /**
     * 重置整个表达式
     *
     * @return this
     */
    Express reset();


    /**
     * 表达式工厂类
     */
    class ExpressFactory {

//        private static final ThreadLocal<Express> expressRef = new ThreadLocal<Express>() {
//            @Override
//            protected Express initialValue() {
//                return new OgnlExpress();
//            }
//        };

        /**
         * 构造表达式执行类
         *
         * @param object 执行对象
         * @return 返回表达式实现
         */
        public static Express newExpress(Object object) {
            return new OgnlExpress().reset().bind(object);
            // return new OgnlExpress().bind(object);
        }

    }

    class OgnlExpress implements Express {

        private Object bindObject;
        private final OgnlContext context = new OgnlContext();

        @Override
        public Object get(String express) throws ExpressException {
            try {
                context.setMemberAccess(new DefaultMemberAccess(true));
                return Ognl.getValue(express, context, bindObject);
            } catch (Exception e) {
                throw new ExpressException(express, e);
            }
        }

        @Override
        public boolean is(String express) throws ExpressException {
            try {
                final Object ret = get(express);
                return null != ret
                        && ret instanceof Boolean
                        && (Boolean) ret;
            } catch (Throwable t) {
                return false;
            }
        }

        @Override
        public Express bind(Object object) {
            this.bindObject = object;
            return this;
        }

        @Override
        public Express bind(String name, Object value) {
            context.put(name, value);
            return this;
        }

        @Override
        public Express reset() {
            context.clear();
            return this;
        }
    }

}
