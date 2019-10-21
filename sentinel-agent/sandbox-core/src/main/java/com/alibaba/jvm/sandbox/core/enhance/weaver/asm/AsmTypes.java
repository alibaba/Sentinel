package com.alibaba.jvm.sandbox.core.enhance.weaver.asm;

import org.objectweb.asm.Type;

import java.com.alibaba.jvm.sandbox.spy.Spy;

/**
 * 常用的ASM type集合
 * 省得我到处声明
 * Created by luanjia@taobao.com on 16/5/21.
 */
public interface AsmTypes {

    Type ASM_TYPE_SPY = Type.getType(Spy.class);
    Type ASM_TYPE_OBJECT = Type.getType(Object.class);
    Type ASM_TYPE_INT = Type.getType(int.class);
    Type ASM_TYPE_SPY_RET = Type.getType(Spy.Ret.class);
    Type ASM_TYPE_THROWABLE = Type.getType(Throwable.class);
    Type ASM_TYPE_CLASS = Type.getType(Class.class);

}
