package com.alibaba.csp.sentinel.adapter.gateway.common.api;

import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class GatewayApiDefinitionManagerTest {

    @Test
    public void testIsValidApi() {
        ApiDefinition bad1 = new ApiDefinition();
        ApiDefinition bad2 = new ApiDefinition("foo");
        ApiDefinition good1 = new ApiDefinition("foo")
            .setPredicateItems(Collections.<ApiPredicateItem>singleton(new ApiPathPredicateItem()
                .setPattern("/abc")
            ));

        assertFalse(GatewayApiDefinitionManager.isValidApi(bad1));
        assertFalse(GatewayApiDefinitionManager.isValidApi(bad2));
        assertTrue(GatewayApiDefinitionManager.isValidApi(good1));
    }
}