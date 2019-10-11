package com.alibaba.csp.sentinel.demo.flow.relate;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.Arrays;

/**
 * Created by zhangyide on 2019-09-23
 */
public class RelationFlowControlDemo {

	static String node_read = "read";
	static String node_write = "write";

	/**
	 * First 10 seconds,the QPS of the write runner is 20. The read runner is not blocked.
	 * In next 20 seconds,the QPS of the write runner changes to 200. The read runner is blocked because the
	 * threshold of relation flow is 30.
	 * Last 30 seconds, the QPS of the write runner becomes 20. The read runner continues running.
	 */
	public static void main(String[] args) {
		relationFlowRules();
		final int threadCount = 20;
		RelationFlowControlReadRunner readRunner = new RelationFlowControlReadRunner(node_read, 60, threadCount);
		readRunner.tick();
		RelationFlowControlWriteRunner writeRunner = new RelationFlowControlWriteRunner(node_write, 60, threadCount);
		writeRunner.tick();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		readRunner.simulateTraffic();
		writeRunner.simulateTraffic();

	}

	private static void relationFlowRules() {
		FlowRule rule = new FlowRule();
		rule.setResource(node_read);
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule.setStrategy(RuleConstant.STRATEGY_RELATE);
		rule.setCount(30);
		rule.setRefResource(node_write);
		FlowRuleManager.loadRules(Arrays.asList(rule));
	}
}
