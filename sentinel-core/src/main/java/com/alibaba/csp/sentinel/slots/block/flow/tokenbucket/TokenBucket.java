package com.alibaba.csp.sentinel.slots.block.flow.tokenbucket;

/**
 * @author LearningGp
 */
public interface TokenBucket {

    boolean tryConsume(long tokenNum);

    void refreshCurrentTokenNum(long timestamp);

}
