/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.controller.gateway;

import com.alibaba.csp.sentinel.dashboard.auth.AuthorizationInterceptor;
import com.alibaba.csp.sentinel.dashboard.auth.FakeAuthServiceImpl;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayParamFlowItemEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.SimpleMachineDiscovery;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule.GatewayParamFlowItemVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemGatewayFlowRuleStore;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.*;
import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

/**
 * Test cases for {@link GatewayFlowRuleController}.
 *
 * @author cdfive
 */
@RunWith(SpringRunner.class)
@WebMvcTest(GatewayFlowRuleController.class)
@Import({FakeAuthServiceImpl.class, InMemGatewayFlowRuleStore.class, AppManagement.class, SimpleMachineDiscovery.class,
        AuthorizationInterceptor.class })
public class GatewayFlowRuleControllerTest {

    private static final String TEST_APP = "test_app";

    private static final String TEST_IP = "localhost";

    private static final Integer TEST_PORT = 8719;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemGatewayFlowRuleStore repository;

    @MockBean
    private SentinelApiClient sentinelApiClient;

    @Before
    public void before() {
        repository.clearAll();
    }

    @Test
    public void testQueryFlowRules() throws Exception {
        String path = "/gateway/flow/list.json";

        List<GatewayFlowRuleEntity> entities = new ArrayList<>();

        // Mock two entities
        GatewayFlowRuleEntity entity = new GatewayFlowRuleEntity();
        entity.setId(1L);
        entity.setApp(TEST_APP);
        entity.setIp(TEST_IP);
        entity.setPort(TEST_PORT);
        entity.setResource("httpbin_route");
        entity.setResourceMode(RESOURCE_MODE_ROUTE_ID);
        entity.setGrade(FLOW_GRADE_QPS);
        entity.setCount(5D);
        entity.setInterval(30L);
        entity.setIntervalUnit(GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND);
        entity.setControlBehavior(CONTROL_BEHAVIOR_DEFAULT);
        entity.setBurst(0);
        entity.setMaxQueueingTimeoutMs(0);

        GatewayParamFlowItemEntity itemEntity = new GatewayParamFlowItemEntity();
        entity.setParamItem(itemEntity);
        itemEntity.setParseStrategy(PARAM_PARSE_STRATEGY_CLIENT_IP);
        entities.add(entity);

        GatewayFlowRuleEntity entity2 = new GatewayFlowRuleEntity();
        entity2.setId(2L);
        entity2.setApp(TEST_APP);
        entity2.setIp(TEST_IP);
        entity2.setPort(TEST_PORT);
        entity2.setResource("some_customized_api");
        entity2.setResourceMode(RESOURCE_MODE_CUSTOM_API_NAME);
        entity2.setCount(30D);
        entity2.setInterval(2L);
        entity2.setIntervalUnit(GatewayFlowRuleEntity.INTERVAL_UNIT_MINUTE);
        entity2.setControlBehavior(CONTROL_BEHAVIOR_DEFAULT);
        entity2.setBurst(0);
        entity2.setMaxQueueingTimeoutMs(0);

        GatewayParamFlowItemEntity itemEntity2 = new GatewayParamFlowItemEntity();
        entity2.setParamItem(itemEntity2);
        itemEntity2.setParseStrategy(PARAM_PARSE_STRATEGY_CLIENT_IP);
        entities.add(entity2);

        CompletableFuture<List<GatewayFlowRuleEntity>> completableFuture = mock(CompletableFuture.class);
        given(completableFuture.get()).willReturn(entities);
        given(sentinelApiClient.fetchGatewayFlowRules(TEST_APP, TEST_IP, TEST_PORT)).willReturn(completableFuture);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(path);
        requestBuilder.param("app", TEST_APP);
        requestBuilder.param("ip", TEST_IP);
        requestBuilder.param("port", String.valueOf(TEST_PORT));

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the fetchGatewayFlowRules method has been called
        verify(sentinelApiClient).fetchGatewayFlowRules(TEST_APP, TEST_IP, TEST_PORT);

        // Verify if two same entities are got
        Result<List<GatewayFlowRuleEntity>> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<List<GatewayFlowRuleEntity>>>(){});
        assertTrue(result.isSuccess());

        List<GatewayFlowRuleEntity> data = result.getData();
        assertEquals(2, data.size());
        assertEquals(entities, data);

        // Verify the entities are add into memory repository
        List<GatewayFlowRuleEntity> entitiesInMem = repository.findAllByApp(TEST_APP);
        assertEquals(2, entitiesInMem.size());
        assertEquals(entities, entitiesInMem);
    }

    @Test
    public void testAddFlowRule() throws Exception {
        String path = "/gateway/flow/new.json";

        AddFlowRuleReqVo reqVo = new AddFlowRuleReqVo();
        reqVo.setApp(TEST_APP);
        reqVo.setIp(TEST_IP);
        reqVo.setPort(TEST_PORT);

        reqVo.setResourceMode(RESOURCE_MODE_ROUTE_ID);
        reqVo.setResource("httpbin_route");

        reqVo.setGrade(FLOW_GRADE_QPS);
        reqVo.setCount(5D);
        reqVo.setInterval(30L);
        reqVo.setIntervalUnit(GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND);
        reqVo.setControlBehavior(CONTROL_BEHAVIOR_DEFAULT);
        reqVo.setBurst(0);
        reqVo.setMaxQueueingTimeoutMs(0);

        given(sentinelApiClient.modifyGatewayFlowRules(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any())).willReturn(true);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
        requestBuilder.content(JSON.toJSONString(reqVo)).contentType(MediaType.APPLICATION_JSON);

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the modifyGatewayFlowRules method has been called
        verify(sentinelApiClient).modifyGatewayFlowRules(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any());

        Result<GatewayFlowRuleEntity> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<GatewayFlowRuleEntity>>() {});
        assertTrue(result.isSuccess());

        // Verify the result
        GatewayFlowRuleEntity entity = result.getData();
        assertNotNull(entity);
        assertEquals(TEST_APP, entity.getApp());
        assertEquals(TEST_IP, entity.getIp());
        assertEquals(TEST_PORT, entity.getPort());
        assertEquals(RESOURCE_MODE_ROUTE_ID, entity.getResourceMode().intValue());
        assertEquals("httpbin_route", entity.getResource());
        assertNotNull(entity.getId());
        assertNotNull(entity.getGmtCreate());
        assertNotNull(entity.getGmtModified());

        // Verify the entity which is add in memory repository
        List<GatewayFlowRuleEntity> entitiesInMem = repository.findAllByApp(TEST_APP);
        assertEquals(1, entitiesInMem.size());
        assertEquals(entity, entitiesInMem.get(0));
    }

    @Test
    public void testUpdateFlowRule() throws Exception {
        String path = "/gateway/flow/save.json";

        // Add one entity into memory repository for update
        GatewayFlowRuleEntity addEntity = new GatewayFlowRuleEntity();
        addEntity.setId(1L);
        addEntity.setApp(TEST_APP);
        addEntity.setIp(TEST_IP);
        addEntity.setPort(TEST_PORT);
        addEntity.setResource("httpbin_route");
        addEntity.setResourceMode(RESOURCE_MODE_ROUTE_ID);
        addEntity.setGrade(FLOW_GRADE_QPS);
        addEntity.setCount(5D);
        addEntity.setInterval(30L);
        addEntity.setIntervalUnit(GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND);
        addEntity.setControlBehavior(CONTROL_BEHAVIOR_DEFAULT);
        addEntity.setBurst(0);
        addEntity.setMaxQueueingTimeoutMs(0);
        Date date = new Date();
        // To make the gmtModified different when do update
        date = DateUtils.addSeconds(date, -1);
        addEntity.setGmtCreate(date);
        addEntity.setGmtModified(date);

        GatewayParamFlowItemEntity addItemEntity = new GatewayParamFlowItemEntity();
        addEntity.setParamItem(addItemEntity);
        addItemEntity.setParseStrategy(PARAM_PARSE_STRATEGY_CLIENT_IP);

        repository.save(addEntity);

        UpdateFlowRuleReqVo reqVo = new UpdateFlowRuleReqVo();
        reqVo.setId(addEntity.getId());
        reqVo.setApp(TEST_APP);
        reqVo.setGrade(FLOW_GRADE_QPS);
        reqVo.setCount(6D);
        reqVo.setInterval(2L);
        reqVo.setIntervalUnit(GatewayFlowRuleEntity.INTERVAL_UNIT_MINUTE);
        reqVo.setControlBehavior(CONTROL_BEHAVIOR_RATE_LIMITER);
        reqVo.setMaxQueueingTimeoutMs(500);

        GatewayParamFlowItemVo itemVo = new GatewayParamFlowItemVo();
        reqVo.setParamItem(itemVo);
        itemVo.setParseStrategy(PARAM_PARSE_STRATEGY_URL_PARAM);
        itemVo.setFieldName("pa");

        given(sentinelApiClient.modifyGatewayFlowRules(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any())).willReturn(true);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
        requestBuilder.content(JSON.toJSONString(reqVo)).contentType(MediaType.APPLICATION_JSON);

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the modifyGatewayFlowRules method has been called
        verify(sentinelApiClient).modifyGatewayFlowRules(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any());

        Result<GatewayFlowRuleEntity> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<GatewayFlowRuleEntity>>() {
        });
        assertTrue(result.isSuccess());

        GatewayFlowRuleEntity entity = result.getData();
        assertNotNull(entity);
        assertEquals(RESOURCE_MODE_ROUTE_ID, entity.getResourceMode().intValue());
        assertEquals("httpbin_route", entity.getResource());
        assertEquals(6D, entity.getCount().doubleValue(), 0);
        assertEquals(2L, entity.getInterval().longValue());
        assertEquals(GatewayFlowRuleEntity.INTERVAL_UNIT_MINUTE, entity.getIntervalUnit().intValue());
        assertEquals(CONTROL_BEHAVIOR_RATE_LIMITER, entity.getControlBehavior().intValue());
        assertEquals(0, entity.getBurst().intValue());
        assertEquals(500, entity.getMaxQueueingTimeoutMs().intValue());
        assertEquals(date, entity.getGmtCreate());
        // To make sure gmtModified has been set and it's different from gmtCreate
        assertNotNull(entity.getGmtModified());
        assertNotEquals(entity.getGmtCreate(), entity.getGmtModified());

        // Verify the entity which is update in memory repository
        GatewayParamFlowItemEntity itemEntity = entity.getParamItem();
        assertEquals(PARAM_PARSE_STRATEGY_URL_PARAM, itemEntity.getParseStrategy().intValue());
        assertEquals("pa", itemEntity.getFieldName());
    }

    @Test
    public void testDeleteFlowRule() throws Exception {
        String path = "/gateway/flow/delete.json";

        // Add one entity into memory repository for delete
        GatewayFlowRuleEntity addEntity = new GatewayFlowRuleEntity();
        addEntity.setId(1L);
        addEntity.setApp(TEST_APP);
        addEntity.setIp(TEST_IP);
        addEntity.setPort(TEST_PORT);
        addEntity.setResource("httpbin_route");
        addEntity.setResourceMode(RESOURCE_MODE_ROUTE_ID);
        addEntity.setGrade(FLOW_GRADE_QPS);
        addEntity.setCount(5D);
        addEntity.setInterval(30L);
        addEntity.setIntervalUnit(GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND);
        addEntity.setControlBehavior(CONTROL_BEHAVIOR_DEFAULT);
        addEntity.setBurst(0);
        addEntity.setMaxQueueingTimeoutMs(0);
        Date date = new Date();
        date = DateUtils.addSeconds(date, -1);
        addEntity.setGmtCreate(date);
        addEntity.setGmtModified(date);

        GatewayParamFlowItemEntity addItemEntity = new GatewayParamFlowItemEntity();
        addEntity.setParamItem(addItemEntity);
        addItemEntity.setParseStrategy(PARAM_PARSE_STRATEGY_CLIENT_IP);

        repository.save(addEntity);

        given(sentinelApiClient.modifyGatewayFlowRules(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any())).willReturn(true);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
        requestBuilder.param("id", String.valueOf(addEntity.getId()));

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the modifyGatewayFlowRules method has been called
        verify(sentinelApiClient).modifyGatewayFlowRules(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any());

        // Verify the result
        Result<Long> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<Long>>() {});
        assertTrue(result.isSuccess());

        assertEquals(addEntity.getId(), result.getData());

        // Now no entities in memory
        List<GatewayFlowRuleEntity> entitiesInMem = repository.findAllByApp(TEST_APP);
        assertEquals(0, entitiesInMem.size());
    }
}
