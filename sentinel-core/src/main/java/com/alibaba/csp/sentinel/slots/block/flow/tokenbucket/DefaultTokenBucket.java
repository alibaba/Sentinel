package com.alibaba.csp.sentinel.slots.block.flow.tokenbucket;

/**
 * @author LearningGp
 */
public class DefaultTokenBucket extends AbstractTokenBucket{

    public DefaultTokenBucket(long unitProduceNum, long maxTokenNum, long intervalInMs){
        super(unitProduceNum, maxTokenNum, false, intervalInMs);
    }

    public DefaultTokenBucket(long unitProduceNum, long maxTokenNum, boolean fullStart, long intervalInMs){
        super(unitProduceNum, maxTokenNum, fullStart, intervalInMs);
    }
}
