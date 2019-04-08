package com.alibaba.csp.sentinel.slots.block.flow.param;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.DefaultProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.SlotChainBuilder;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.HotParamSlotChainBuilder;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.authority.AuthoritySlot;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot;
import com.alibaba.csp.sentinel.slots.block.flow.FlowSlot;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.slots.logger.LogSlot;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlot;
import com.alibaba.csp.sentinel.slots.system.SystemSlot;
import com.alibaba.csp.sentinel.util.TimeUtil;

public class ParamFlowCheckerWithBurstTest {
	private final ParamFlowSlot paramFlowSlot = new ParamFlowSlot();
	final String resA = "resA";
	ResourceWrapper resourceWrapper = new StringResourceWrapper(resA, EntryType.IN);

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {
		ParamFlowRuleManager.loadRules(null);
	}

	



}
