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

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.config.NoAuthConfigurationTest;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiPredicateItemEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.SimpleMachineDiscovery;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api.AddApiReqVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api.ApiPredicateItemVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api.UpdateApiReqVo;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemApiDefinitionStore;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.time.DateUtils;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT;
import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

/**
 * Test cases for {@link GatewayApiController}.
 *
 * @author cdfive
 */
@RunWith(SpringRunner.class)
@WebMvcTest(GatewayApiController.class)
@Import({NoAuthConfigurationTest.class, InMemApiDefinitionStore.class, AppManagement.class, SimpleMachineDiscovery.class})
public class GatewayApiControllerTest {

    private static final String TEST_APP = "test_app";

    private static final String TEST_IP = "localhost";

    private static final Integer TEST_PORT = 8719;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemApiDefinitionStore repository;

    @MockBean
    private SentinelApiClient sentinelApiClient;

    @Before
    public void before() {
        repository.clearAll();
    }

    @Test
    public void testQueryApis() throws Exception {
        String path = "/gateway/api/list.json";

        List<ApiDefinitionEntity> entities = new ArrayList<>();

        // Mock two entities
        ApiDefinitionEntity entity = new ApiDefinitionEntity();
        entity.setId(1L);
        entity.setApp(TEST_APP);
        entity.setIp(TEST_IP);
        entity.setPort(TEST_PORT);
        entity.setApiName("foo");
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);

        Set<ApiPredicateItemEntity> itemEntities = new LinkedHashSet<>();
        entity.setPredicateItems(itemEntities);
        ApiPredicateItemEntity itemEntity = new ApiPredicateItemEntity();
        itemEntity.setPattern("/aaa");
        itemEntity.setMatchStrategy(URL_MATCH_STRATEGY_EXACT);

        itemEntities.add(itemEntity);
        entities.add(entity);

        ApiDefinitionEntity entity2 = new ApiDefinitionEntity();
        entity2.setId(2L);
        entity2.setApp(TEST_APP);
        entity2.setIp(TEST_IP);
        entity2.setPort(TEST_PORT);
        entity2.setApiName("biz");
        entity.setGmtCreate(date);
        entity.setGmtModified(date);

        Set<ApiPredicateItemEntity> itemEntities2 = new LinkedHashSet<>();
        entity2.setPredicateItems(itemEntities2);
        ApiPredicateItemEntity itemEntity2 = new ApiPredicateItemEntity();
        itemEntity2.setPattern("/bbb");
        itemEntity2.setMatchStrategy(URL_MATCH_STRATEGY_PREFIX);

        itemEntities2.add(itemEntity2);
        entities.add(entity2);

        CompletableFuture<List<ApiDefinitionEntity>> completableFuture = mock(CompletableFuture.class);
        given(completableFuture.get()).willReturn(entities);
        given(sentinelApiClient.fetchApis(TEST_APP, TEST_IP, TEST_PORT)).willReturn(completableFuture);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(path);
        requestBuilder.param("app", TEST_APP);
        requestBuilder.param("ip", TEST_IP);
        requestBuilder.param("port", String.valueOf(TEST_PORT));

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the fetchApis method has been called
        verify(sentinelApiClient).fetchApis(TEST_APP, TEST_IP, TEST_PORT);

        // Verify if two same entities are got
        Result<List<ApiDefinitionEntity>> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<List<ApiDefinitionEntity>>>(){});
        assertTrue(result.isSuccess());

        List<ApiDefinitionEntity> data = result.getData();
        assertEquals(2, data.size());
        assertEquals(entities, data);

        // Verify the entities are add into memory repository
        List<ApiDefinitionEntity> entitiesInMem = repository.findAllByApp(TEST_APP);
        assertEquals(2, entitiesInMem.size());
        assertEquals(entities, entitiesInMem);
    }

    @Test
    public void testAddApi() throws Exception {
        String path = "/gateway/api/new.json";

        AddApiReqVo reqVo = new AddApiReqVo();
        reqVo.setApp(TEST_APP);
        reqVo.setIp(TEST_IP);
        reqVo.setPort(TEST_PORT);

        reqVo.setApiName("customized_api");

        List<ApiPredicateItemVo> itemVos = new ArrayList<>();
        ApiPredicateItemVo itemVo = new ApiPredicateItemVo();
        itemVo.setMatchStrategy(URL_MATCH_STRATEGY_EXACT);
        itemVo.setPattern("/product");
        itemVos.add(itemVo);
        reqVo.setPredicateItems(itemVos);

        given(sentinelApiClient.modifyApis(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any())).willReturn(true);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
        requestBuilder.content(JSON.toJSONString(reqVo)).contentType(MediaType.APPLICATION_JSON);

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the modifyApis method has been called
        verify(sentinelApiClient).modifyApis(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any());

        Result<ApiDefinitionEntity> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<ApiDefinitionEntity>>() {});
        assertTrue(result.isSuccess());

        // Verify the result
        ApiDefinitionEntity entity = result.getData();
        assertNotNull(entity);
        assertEquals(TEST_APP, entity.getApp());
        assertEquals(TEST_IP, entity.getIp());
        assertEquals(TEST_PORT, entity.getPort());
        assertEquals("customized_api", entity.getApiName());
        assertNotNull(entity.getId());
        assertNotNull(entity.getGmtCreate());
        assertNotNull(entity.getGmtModified());

        Set<ApiPredicateItemEntity> predicateItemEntities = entity.getPredicateItems();
        assertEquals(1, predicateItemEntities.size());
        ApiPredicateItemEntity predicateItemEntity = predicateItemEntities.iterator().next();
        assertEquals(URL_MATCH_STRATEGY_EXACT, predicateItemEntity.getMatchStrategy().intValue());
        assertEquals("/product", predicateItemEntity.getPattern());

        // Verify the entity which is add in memory repository
        List<ApiDefinitionEntity> entitiesInMem = repository.findAllByApp(TEST_APP);
        assertEquals(1, entitiesInMem.size());
        assertEquals(entity, entitiesInMem.get(0));
    }

    @Test
    public void testUpdateApi() throws Exception {
        String path = "/gateway/api/save.json";

        // Add one entity to memory repository for update
        ApiDefinitionEntity addEntity = new ApiDefinitionEntity();
        addEntity.setApp(TEST_APP);
        addEntity.setIp(TEST_IP);
        addEntity.setPort(TEST_PORT);
        addEntity.setApiName("bbb");
        Date date = new Date();
        // To make the gmtModified different when do update
        date = DateUtils.addSeconds(date, -1);
        addEntity.setGmtCreate(date);
        addEntity.setGmtModified(date);
        Set<ApiPredicateItemEntity> addRedicateItemEntities = new HashSet<>();
        addEntity.setPredicateItems(addRedicateItemEntities);
        ApiPredicateItemEntity addPredicateItemEntity = new ApiPredicateItemEntity();
        addPredicateItemEntity.setMatchStrategy(URL_MATCH_STRATEGY_EXACT);
        addPredicateItemEntity.setPattern("/order");
        addEntity = repository.save(addEntity);

        UpdateApiReqVo reqVo = new UpdateApiReqVo();
        reqVo.setId(addEntity.getId());
        reqVo.setApp(TEST_APP);
        List<ApiPredicateItemVo> itemVos = new ArrayList<>();
        ApiPredicateItemVo itemVo = new ApiPredicateItemVo();
        itemVo.setMatchStrategy(URL_MATCH_STRATEGY_PREFIX);
        itemVo.setPattern("/my_order");
        itemVos.add(itemVo);
        reqVo.setPredicateItems(itemVos);

        given(sentinelApiClient.modifyApis(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any())).willReturn(true);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
        requestBuilder.content(JSON.toJSONString(reqVo)).contentType(MediaType.APPLICATION_JSON);

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the modifyApis method has been called
        verify(sentinelApiClient).modifyApis(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any());

        Result<ApiDefinitionEntity> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<ApiDefinitionEntity>>() {});
        assertTrue(result.isSuccess());

        ApiDefinitionEntity entity = result.getData();
        assertNotNull(entity);
        assertEquals("bbb", entity.getApiName());
        assertEquals(date, entity.getGmtCreate());
        // To make sure gmtModified has been set and it's different from gmtCreate
        assertNotNull(entity.getGmtModified());
        assertNotEquals(entity.getGmtCreate(), entity.getGmtModified());

        Set<ApiPredicateItemEntity> predicateItemEntities = entity.getPredicateItems();
        assertEquals(1, predicateItemEntities.size());
        ApiPredicateItemEntity predicateItemEntity = predicateItemEntities.iterator().next();
        assertEquals(URL_MATCH_STRATEGY_PREFIX, predicateItemEntity.getMatchStrategy().intValue());
        assertEquals("/my_order", predicateItemEntity.getPattern());

        // Verify the entity which is update in memory repository
        List<ApiDefinitionEntity> entitiesInMem = repository.findAllByApp(TEST_APP);
        assertEquals(1, entitiesInMem.size());
        assertEquals(entity, entitiesInMem.get(0));
    }

    @Test
    public void testDeleteApi() throws Exception {
        String path = "/gateway/api/delete.json";

        // Add one entity into memory repository for delete
        ApiDefinitionEntity addEntity = new ApiDefinitionEntity();
        addEntity.setApp(TEST_APP);
        addEntity.setIp(TEST_IP);
        addEntity.setPort(TEST_PORT);
        addEntity.setApiName("ccc");
        Date date = new Date();
        addEntity.setGmtCreate(date);
        addEntity.setGmtModified(date);
        Set<ApiPredicateItemEntity> addRedicateItemEntities = new HashSet<>();
        addEntity.setPredicateItems(addRedicateItemEntities);
        ApiPredicateItemEntity addPredicateItemEntity = new ApiPredicateItemEntity();
        addPredicateItemEntity.setMatchStrategy(URL_MATCH_STRATEGY_EXACT);
        addPredicateItemEntity.setPattern("/user/add");
        addEntity = repository.save(addEntity);

        given(sentinelApiClient.modifyApis(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any())).willReturn(true);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(path);
        requestBuilder.param("id", String.valueOf(addEntity.getId()));

        // Do controller logic
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();

        // Verify the modifyApis method has been called
        verify(sentinelApiClient).modifyApis(eq(TEST_APP), eq(TEST_IP), eq(TEST_PORT), any());

        // Verify the result
        Result<Long> result = JSONObject.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<Long>>() {});
        assertTrue(result.isSuccess());

        assertEquals(addEntity.getId(), result.getData());

        // Now no entities in memory
        List<ApiDefinitionEntity> entitiesInMem = repository.findAllByApp(TEST_APP);
        assertEquals(0, entitiesInMem.size());
    }
}
