package com.alibaba.csp.sentinel.demo.flow.relate;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.Arrays;

/**
 * Created by zhangyide on 2019-09-23
 */
public class FlowQpsRelateDemo {

	static String node_read = "read";
	static String node_write = "write";

	public static void main(String[] args) {
		initParamFlowRules();
		final int threadCount = 20;
		FlowQpsRelateReadRunner readRunner = new FlowQpsRelateReadRunner(node_read, 60, threadCount);
		readRunner.tick();
		FlowQpsRelateWriteRunner writeRunner = new FlowQpsRelateWriteRunner(node_write, 60, threadCount);
		writeRunner.tick();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		readRunner.simulateTraffic();
		writeRunner.simulateTraffic();

	}

	private static void initParamFlowRules() {
		FlowRule rule = new FlowRule();
		rule.setResource(node_read);
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule.setStrategy(RuleConstant.STRATEGY_RELATE);
		rule.setCount(30);
		rule.setRefResource(node_write);
		FlowRuleManager.loadRules(Arrays.asList(rule));
	}
}
