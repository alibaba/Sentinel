package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alipay.sofa.rpc.codec.Serializer;
import com.alipay.sofa.rpc.common.RpcConfigs;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Test cases for {@link AbstractSofaRpcFilter}.
 *
 * @author cdfive
 */
public class AbstractSofaRpcFilterTest {

    @Before
    public void setUp() {
        removeRpcConfig(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED);
    }

    @After
    public void cleanUp() {
        removeRpcConfig(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED);
    }

    @Test
    public void testNeedToLoadProvider() {
        SentinelSofaRpcProviderFilter providerFilter = new SentinelSofaRpcProviderFilter();
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setInterfaceId(Serializer.class.getName());
        providerConfig.setId("AAA");
        FilterInvoker invoker = new FilterInvoker(null, null, providerConfig);
        assertTrue(providerFilter.needToLoad(invoker));

        providerConfig.setParameter(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "false");
        assertFalse(providerFilter.needToLoad(invoker));

        providerConfig.setParameter(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "");
        assertTrue(providerFilter.needToLoad(invoker));

        RpcConfigs.putValue(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "false");
        assertFalse(providerFilter.needToLoad(invoker));
    }

    @Test
    public void testNeedToLoadConsumer() {
        SentinelSofaRpcConsumerFilter consumerFilter = new SentinelSofaRpcConsumerFilter();
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setInterfaceId(Serializer.class.getName());
        consumerConfig.setId("BBB");
        FilterInvoker invoker = new FilterInvoker(null, null, consumerConfig);
        assertTrue(consumerFilter.needToLoad(invoker));

        consumerConfig.setParameter(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "false");
        assertFalse(consumerFilter.needToLoad(invoker));

        consumerConfig.setParameter(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "");
        assertTrue(consumerFilter.needToLoad(invoker));

        RpcConfigs.putValue(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "false");
        assertFalse(consumerFilter.needToLoad(invoker));
    }

    @Test
    public void testNeedToLoadProviderAndConsumer() {
        SentinelSofaRpcProviderFilter providerFilter = new SentinelSofaRpcProviderFilter();
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setInterfaceId(Serializer.class.getName());
        providerConfig.setId("AAA");
        FilterInvoker providerInvoker = new FilterInvoker(null, null, providerConfig);
        assertTrue(providerFilter.needToLoad(providerInvoker));

        SentinelSofaRpcConsumerFilter consumerFilter = new SentinelSofaRpcConsumerFilter();
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setInterfaceId(Serializer.class.getName());
        consumerConfig.setId("BBB");
        FilterInvoker consumerInvoker = new FilterInvoker(null, null, consumerConfig);
        assertTrue(consumerFilter.needToLoad(consumerInvoker));

        providerConfig.setParameter(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "false");
        assertFalse(providerFilter.needToLoad(providerInvoker));
        assertTrue(consumerFilter.needToLoad(consumerInvoker));

        providerConfig.setParameter(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "");
        assertTrue(providerFilter.needToLoad(providerInvoker));

        RpcConfigs.putValue(SentinelConstants.SOFA_RPC_SENTINEL_ENABLED, "false");
        assertFalse(providerFilter.needToLoad(providerInvoker));
        assertFalse(consumerFilter.needToLoad(consumerInvoker));
    }

    private void removeRpcConfig(String key) {
        try {
            Method removeValueMethod = RpcConfigs.class.getDeclaredMethod("removeValue", String.class);
            removeValueMethod.setAccessible(true);
            removeValueMethod.invoke(null, key);
        } catch (Exception e) {
            // Empty
        }
    }
}
