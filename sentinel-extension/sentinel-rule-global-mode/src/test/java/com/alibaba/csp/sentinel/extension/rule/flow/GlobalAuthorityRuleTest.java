package com.alibaba.csp.sentinel.extension.rule.flow;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.extension.rule.authority.GlobalAuthorityRule;
import com.alibaba.csp.sentinel.extension.rule.authority.GlobalAuthorityRulePropertyListener;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertFalse;

/**
 * @author : jiez
 * @date : 2021/7/22 16:42
 */
public class GlobalAuthorityRuleTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testPropertyListenerLoad() {
        AssertUtil.isTrue(AuthorityRuleManager.getListener().getClass() == GlobalAuthorityRulePropertyListener.class, "authority rule property listener load fail");
    }

    @Test
    public void testWhiteBlock() {
        String origin = "appA";
        GlobalAuthorityRule ruleA = new GlobalAuthorityRule();
        ruleA.setResource("[a-z]*")
                .setLimitApp(origin + ",appB")
                .as(GlobalAuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.loadRules(Arrays.asList(ruleA));

        ContextUtil.enter("entrance", "appA,");
        assertFalse(entry("test"));
        ContextUtil.exit();
    }
    @Test
    public void testWhitePass() throws BlockException {
        String origin = "appA";
        String resourceName = "testPassCheck";
        GlobalAuthorityRule ruleA = new GlobalAuthorityRule();
        ruleA.setResource("[a-z]*")
                .setLimitApp(origin + ",appB")
                .as(GlobalAuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.loadRules(Arrays.asList(ruleA));

        ContextUtil.enter("entrance", "appA");
        Entry entry = SphU.entry("test");
        entry.exit();
        ContextUtil.exit();
    }


    @Test
    public void testWhitePassWithNotMatchRule() throws BlockException {
        String origin = "test";
        String resourceName = "testPassCheck";
        GlobalAuthorityRule ruleA = new GlobalAuthorityRule();
        ruleA.setResource("test")
                .setLimitApp(origin + ",appB")
                .as(GlobalAuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.loadRules(Arrays.asList(ruleA));

        ContextUtil.enter("entrance", "test");
        Entry entry = SphU.entry("test2");
        entry.exit();
        ContextUtil.exit();
    }

    @Test
    public void testBlackBlock() throws BlockException {
        String origin = "appA";
        String resourceName = "testPassCheck";
        GlobalAuthorityRule ruleA = new GlobalAuthorityRule();
        ruleA.setResource("[a-z]*")
                .setLimitApp(origin + ",appB")
                .as(GlobalAuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK);
        AuthorityRuleManager.loadRules(Arrays.asList(ruleA));

        ContextUtil.enter("entrance", "appA");
        assertFalse(entry("test"));
        ContextUtil.exit();
    }

    @Test
    public void testBlackPassWithNotMatchRule() throws BlockException {
        String origin = "test";
        String resourceName = "testPassCheck";
        GlobalAuthorityRule ruleA = new GlobalAuthorityRule();
        ruleA.setResource("test")
                .setLimitApp(origin + ",appB")
                .as(GlobalAuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK);
        AuthorityRuleManager.loadRules(Arrays.asList(ruleA));

        ContextUtil.enter("entrance", "test");
        Entry entry = SphU.entry("test2");
        entry.exit();
        ContextUtil.exit();
    }

    @Test
    public void testBlackPass() throws BlockException {
        String origin = "appA";
        GlobalAuthorityRule ruleA = new GlobalAuthorityRule();
        ruleA.setResource("[a-z]*")
                .setLimitApp(origin + ",appB")
                .as(GlobalAuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK);
        AuthorityRuleManager.loadRules(Arrays.asList(ruleA));

        ContextUtil.enter("entrance", "appC");
        Entry entry = SphU.entry("test");
        entry.exit();
        ContextUtil.exit();
    }

    protected final boolean entry(String res) {
        Entry entry = null;
        try {
            entry = SphU.entry(res);
        } catch (BlockException b) {
            return false;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        return true;
    }

}
