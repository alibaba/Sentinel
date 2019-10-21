package com.alibaba.jvm.sandbox.core.util.collection;

/**
 * 堆栈
 * Created by luanjia@taobao.com on 15/6/21.
 *
 * @param <E> 堆栈元素类型
 */
public interface GaStack<E> {

    E pop();

    void push(E e);

    E peek();

    boolean isEmpty();

    boolean isLast();

    E peekLast();

    int deep();

}
