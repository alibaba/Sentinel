package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;

/**
 * Rules for controlling adaptive circuit breakers.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeRule extends AbstractRule {

    private boolean enabled = true;

    public AdaptiveDegradeRule(String name) {
        super();
        setResource(name);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdaptiveDegradeRule)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AdaptiveDegradeRule that = (AdaptiveDegradeRule) o;

        return enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}