package com.alibaba.csp.sentinel.slots.block.authority;

import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link AuthorityRuleManager}.
 *
 * @author Eric Zhao
 */
public class AuthorityRuleManagerTest {

    @After
    public void setUp() {
        AuthorityRuleManager.loadRules(null);
    }

    @Test
    public void testLoadRules() {
        String resourceName = "testLoadRules";

        AuthorityRule rule = new AuthorityRule();
        rule.setResource(resourceName);
        rule.setLimitApp("a,b");
        rule.setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.loadRules(Collections.singletonList(rule));

        List<AuthorityRule> rules = AuthorityRuleManager.getRules();
        assertEquals(1, rules.size());
        assertEquals(rule, rules.get(0));

        AuthorityRuleManager.loadRules(Collections.singletonList(new AuthorityRule()));
        rules = AuthorityRuleManager.getRules();
        assertEquals(0, rules.size());
    }

    @Test
    public void testIsValidRule() {
        AuthorityRule ruleA = new AuthorityRule();
        AuthorityRule ruleB = null;
        AuthorityRule ruleC = new AuthorityRule();
        ruleC.setResource("abc");
        AuthorityRule ruleD = new AuthorityRule();
        ruleD.setResource("bcd").setLimitApp("abc");

        assertFalse(AuthorityRuleManager.isValidRule(ruleA));
        assertFalse(AuthorityRuleManager.isValidRule(ruleB));
        assertFalse(AuthorityRuleManager.isValidRule(ruleC));
        assertTrue(AuthorityRuleManager.isValidRule(ruleD));
    }
    @Test
    public void appendAndReplaceRules(){
        String resourceName = "testappendAndReplaceRules";

        AuthorityRule rule = new AuthorityRule();
        rule.setResource(resourceName);
        rule.setLimitApp("a,b");
        rule.setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.loadRules(Collections.singletonList(rule));


        AuthorityRule replace_rule = new AuthorityRule();
        replace_rule.setResource(resourceName);
        replace_rule.setLimitApp("a,b");
        replace_rule.setStrategy(RuleConstant.AUTHORITY_BLACK);
        //replace
        AuthorityRuleManager.appendAndReplaceRules(Collections.singletonList(replace_rule));
        List<AuthorityRule> replacedRules =  AuthorityRuleManager.getRules();
        assertEquals(1,replacedRules.size());
        assertEquals(RuleConstant.AUTHORITY_BLACK,
                replacedRules.get(0).getStrategy());

        AuthorityRule replace_rule_with_dif_limitapp = new AuthorityRule();
        replace_rule_with_dif_limitapp.setResource(resourceName);
        replace_rule_with_dif_limitapp.setLimitApp("a,b,c");
        replace_rule_with_dif_limitapp.setStrategy(RuleConstant.AUTHORITY_BLACK);
        //replace
        AuthorityRuleManager.appendAndReplaceRules(Collections.singletonList(replace_rule_with_dif_limitapp));
        replacedRules =  AuthorityRuleManager.getRules();
        assertEquals(1, replacedRules.size());
        assertEquals("a,b,c", replacedRules.get(0).getLimitApp());

        AuthorityRule append_rule = new AuthorityRule();
        append_rule.setResource("appendRules");
        append_rule.setLimitApp("abcd");
        append_rule.setStrategy(RuleConstant.AUTHORITY_BLACK);
        //append
        AuthorityRuleManager.appendAndReplaceRules(Collections.singletonList(append_rule));

        List<AuthorityRule> appended_rules =  AuthorityRuleManager.getRules();
        assertEquals(2, appended_rules.size());
        assertTrue(appended_rules.stream().anyMatch(item->item.getLimitApp().equals("abcd")));

    }
    @Test
    public void deleteRules(){
        String resourceName = "testappendAndReplaceRules";

        AuthorityRule rule = new AuthorityRule();
        rule.setResource(resourceName);
        rule.setLimitApp("a,b");
        rule.setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.loadRules(Collections.singletonList(rule));


        //delete not exists

        AuthorityRule del_not_exist = new AuthorityRule();
        del_not_exist.setResource(resourceName+"not_exists");
        del_not_exist.setLimitApp("a,b");
        del_not_exist.setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.deleteRules(Collections.singletonList(del_not_exist));
        assertEquals(1,AuthorityRuleManager.getRules().size());

        //delete same resource but limit not same
        AuthorityRule del_same_resource = new AuthorityRule();
        del_same_resource.setResource(resourceName);
        del_same_resource.setLimitApp("a,b"+"not_exists");
        del_same_resource.setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.deleteRules(Collections.singletonList(del_same_resource));
        assertTrue(AuthorityRuleManager.getRules().isEmpty());
    }
    @After
    public void tearDown() {
        AuthorityRuleManager.loadRules(null);
    }
}