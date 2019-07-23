package com.alibaba.csp.sentinel.slots.block.adaptive;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;

/**
 * @author Liu Yiming
 * @date 2019-07-16 16:31
 */
public class AdaptiveRule extends AbstractRule {

    public AdaptiveRule() {}

    public AdaptiveRule(String resourceName) {
        setResource(resourceName);
    }

    private double count;

    private int maxToken;

    private double targetRadio;

    public double getCount() {
        return count;
    }

    public AdaptiveRule setCount(double count) {
        this.count = count;
        return this;
    }

    public int getMaxToken() { return maxToken; }

    public AdaptiveRule setMaxToken(int maxToken) {
        this.maxToken = maxToken;
        return this;
    }

    public double getTargetRadio() { return targetRadio; }

    public AdaptiveRule setTargetRadio(double targetRadio) {
        this.targetRadio = targetRadio;
        return this;
    }

    @Override
    public boolean passCheck(Context context, DefaultNode node, int acquireCount, Object... args) { return true; }

}
