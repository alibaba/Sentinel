package com.alibaba.jvm.sandbox.core.util.collection;

import java.util.NoSuchElementException;

import static java.lang.System.arraycopy;

/**
 * 线程不安全不固定栈深的堆栈实现
 * 比默认的实现带来3倍的性能提升
 * Created by luanjia@taobao.com on 15/6/21.
 *
 * @param <E> 堆栈元素类型
 */
public class ThreadUnsafeGaStack<E> implements GaStack<E> {

    private final static int EMPTY_INDEX = -1;
    private final static int DEFAULT_STACK_DEEP = 12;

    private Object[] elementArray;
    private int current = EMPTY_INDEX;

    public ThreadUnsafeGaStack() {
        this(DEFAULT_STACK_DEEP);
    }

    public ThreadUnsafeGaStack(int stackSize) {
        this.elementArray = new Object[stackSize];
    }


    /**
     * 自动扩容
     * 当前堆栈最大深度不满足期望时会自动扩容(2倍扩容)
     *
     * @param expectDeep 期望堆栈深度
     */
    private void ensureCapacityInternal(int expectDeep) {
        final int currentStackSize = elementArray.length;
        if (elementArray.length <= expectDeep) {
            final Object[] newElementArray = new Object[currentStackSize * 2];
            arraycopy(elementArray, 0, newElementArray, 0, currentStackSize);
            this.elementArray = newElementArray;
        }
    }

    private void checkForPopOrPeek() {
        // stack is empty
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E pop() {
        checkForPopOrPeek();
        //noinspection unchecked
        final E e = (E) elementArray[current];
        elementArray[current] = null;
        current--;
        return e;
    }

    @Override
    public void push(E e) {
        ensureCapacityInternal(current + 1);
        elementArray[++current] = e;
    }

    @Override
    public E peek() {
        checkForPopOrPeek();
        //noinspection unchecked
        return (E) elementArray[current];
    }

    @Override
    public boolean isEmpty() {
        return current == EMPTY_INDEX;
    }

    @Override
    public boolean isLast() {
        return current == 0;
    }

    @Override
    public E peekLast() {
        checkForPopOrPeek();
        return (E) elementArray[0];
    }

    @Override
    public int deep() {
        return current + 1;
    }

    public Object[] getElementArray() {
        return elementArray;
    }

    public int getCurrent() {
        return current;
    }
}
