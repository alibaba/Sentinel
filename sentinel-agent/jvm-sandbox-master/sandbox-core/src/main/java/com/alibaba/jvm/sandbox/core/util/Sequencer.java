package com.alibaba.jvm.sandbox.core.util;

import java.com.alibaba.jvm.sandbox.spy.Spy;

/**
 * 序列发生器
 * 序列发生器用途非常广泛,主要用于圈定全局唯一性标识
 *
 * @author luanjia@taobao.com
 */
public class Sequencer {

    /**
     * 生成下一条序列
     *
     * @return 下一条序列
     */
    public int next() {
        // 这里直接修改为引用Spy的全局唯一序列，修复 #125
        return Spy.nextSequence();
    }

}
