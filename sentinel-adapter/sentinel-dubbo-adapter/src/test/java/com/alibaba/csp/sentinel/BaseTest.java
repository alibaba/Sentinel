package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.dubbo.rpc.RpcContext;

/**
 * Base test class, provide common methods for subClass
 * The package is same as CtSph, to call CtSph.resetChainMap() method for test
 *
 * Note: Only for test. DO NOT USE IN PRODUCTION!
 *
 * @author cdfive
 */
public class BaseTest {

    /**
     * Clean up resources for context, clusterNodeMap, processorSlotChainMap
     */
    protected static void cleanUpAll() {
        RpcContext.removeContext();
        ClusterBuilderSlot.getClusterNodeMap().clear();
        CtSph.resetChainMap();
    }
}