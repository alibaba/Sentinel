package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.dubbo.rpc.RpcContext;

/**
 * Base test class, provide common methods for subClass
 * Note: the package is same as CtSph, to call CtSph.resetChainMap() method for test
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