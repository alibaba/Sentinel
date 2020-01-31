package com.alibaba.csp.sentinel.adapter.spring.webflux;

import java.util.ArrayList;
import java.util.Collections;

import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import com.alibaba.csp.sentinel.adapter.spring.webflux.test.WebFluxTestApplication;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebFluxTestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class SentinelWebFluxIntegrationTest {

    private static final String HELLO_STR = "Hello!";
    private static final String BLOCK_MSG_PREFIX = "Blocked by Sentinel: ";

    @Autowired
    private WebTestClient webClient;

    private void configureRulesFor(String resource, int count) {
        configureRulesFor(resource, count, "default");
    }

    private void configureRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule()
            .setCount(count)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    @Test
    public void testWebFluxFilterBasic() throws Exception {
        String url = "/hello";
        this.webClient.get()
            .uri(url)
            .accept(MediaType.TEXT_PLAIN)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo(HELLO_STR);

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testCustomizedUrlCleaner() throws Exception {
        final String fooPrefix = "/foo/";
        String url1 = fooPrefix + 1;
        String url2 = fooPrefix + 2;
        WebFluxCallbackManager.setUrlCleaner(((exchange, originUrl) -> {
            if (originUrl.startsWith(fooPrefix)) {
                return "/foo/*";
            }
            return originUrl;
        }));
        this.webClient.get()
            .uri(url1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello 1");
        this.webClient.get()
            .uri(url2)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello 2");

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(fooPrefix + "*");
        assertEquals(2, cn.passQps(), 0.01);
        assertNull(ClusterBuilderSlot.getClusterNode(url1));
        assertNull(ClusterBuilderSlot.getClusterNode(url2));

        WebFluxCallbackManager.resetUrlCleaner();
    }

    @Test
    public void testCustomizedIgnoreUrlCleaner() throws Exception {
        final String fooPrefix = "/foo/";
        String url1 = fooPrefix + 1;
        WebFluxCallbackManager.setUrlCleaner(((exchange, originUrl) -> {
            if (originUrl.startsWith(fooPrefix)) {
                return "";
            }
            return originUrl;
        }));
        this.webClient.get()
                .uri(url1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello 1");

        assertNull(ClusterBuilderSlot.getClusterNode(url1));
        WebFluxCallbackManager.resetUrlCleaner();
    }

    @Test
    public void testCustomizedBlockRequestHandler() throws Exception {
        String url = "/error";
        String prefix = "blocked: ";
        WebFluxCallbackManager.setBlockHandler((exchange, t) -> ServerResponse.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .syncBody(prefix + t.getMessage()));

        this.webClient.get()
            .uri(url)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).value(StringContains.containsString(prefix));

        WebFluxCallbackManager.resetBlockHandler();
    }

    @Test
    public void testCustomizedRequestOriginParser() throws Exception {
        String url = "/hello";
        String limitOrigin = "userA";
        final String headerName = "S-User";
        configureRulesFor(url, 0, limitOrigin);

        WebFluxCallbackManager.setRequestOriginParser(exchange -> {
            String origin = exchange.getRequest().getHeaders().getFirst(headerName);
            return origin != null ? origin : "";
        });

        this.webClient.get()
            .uri(url)
            .accept(MediaType.TEXT_PLAIN)
            .header(headerName, "userB")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo(HELLO_STR);
        // This will be blocked.
        this.webClient.get()
            .uri(url)
            .accept(MediaType.TEXT_PLAIN)
            .header(headerName, limitOrigin)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectBody(String.class).value(StringContains.containsString(BLOCK_MSG_PREFIX));
        this.webClient.get()
            .uri(url)
            .accept(MediaType.TEXT_PLAIN)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo(HELLO_STR);

        WebFluxCallbackManager.resetRequestOriginParser();
    }

    @Before
    public void setUp() {
        FlowRuleManager.loadRules(new ArrayList<>());
        ClusterBuilderSlot.resetClusterNodes();
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(new ArrayList<>());
        ClusterBuilderSlot.resetClusterNodes();
    }
}
