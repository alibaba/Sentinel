package com.alibaba.csp.sentinel.slots.block.flow;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Weihua
 * @since 1.0.0
 */
public class FlowRuleManagerTest {

    public static final List<FlowRule> STATIC_RULES_1 = new ArrayList<>();
    public static final List<FlowRule> STATIC_RULES_2 = new ArrayList<>();

    static {
        FlowRule first = new FlowRule();
        first.setResource("/a/b/c");
        first.setCount(100);
        STATIC_RULES_1.add(first);

        FlowRule second = new FlowRule();
        second.setResource("/a/b/c");
        second.setCount(200);
        STATIC_RULES_2.add(second);

    }

    @Test
    public void testLoadAndGetRules(){
        FlowRuleManager.loadRules(STATIC_RULES_1);
        assertEquals(1, FlowRuleManager.getRules().size()); // the initial size
        new Thread(loader, "Loader").start();

        for(int i = 0; i < 10000; i++){
            System.out.println("main:" + i);
            //The initial size is 1, and the size after updating should also be 1,
            //if the actual size is 0, that must be called after clear(),
            // but before putAll() in FlowPropertyListener.configUpdate
            assertEquals(1, FlowRuleManager.getRules().size());
        }

    }

    public Runnable loader = new Runnable() {
        @Override
        public void run() {
            for(int i = 0; i < 10000; i++){
                //to guarantee that they're different and change happens
                System.out.println("loader:" + i);
                FlowRuleManager.loadRules(i%2 == 0 ? STATIC_RULES_2 : STATIC_RULES_1);
            }
        }
    };

}
