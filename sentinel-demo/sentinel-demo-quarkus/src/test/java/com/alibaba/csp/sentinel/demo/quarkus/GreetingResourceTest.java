/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.quarkus;

import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @AfterEach
    public void cleanUp() {
        ClusterBuilderSlot.resetClusterNodes();
    }

    @Test
    public void testSentinelJaxRsQuarkusAdapter() {
        given()
            .when().get("/hello/txt")
            .then()
            .statusCode(200)
            .body(is("hello"));
        given()
            .when().get("/hello/txt")
            .then()
            .statusCode(javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS.getStatusCode())
            .body(is("Blocked by Sentinel (flow limiting)"));
    }

    @Test
    public void testSentinelAnnotationQuarkusAdapter() {
        given()
            .when().get("/hello/fallback/a")
            .then()
            .statusCode(200)
            .body(is("hello a"));
        given()
            .when().get("/hello/fallback/b")
            .then()
            .statusCode(200)
            .body(is("hello b"));
        given()
            .when().get("/hello/fallback/degrade")
            .then()
            .statusCode(200)
            .body(is("globalDefaultFallback, ex:test sentinel fallback"));
        given()
            .when().get("/hello/fallback/degrade")
            .then()
            .statusCode(200)
            .body(is("globalDefaultFallback, ex:test sentinel fallback"));
        given()
            .when().get("/hello/fallback/degrade")
            .then()
            .statusCode(200)
            .body(is("globalBlockHandler, ex:null"));
        given()
            .when().get("/hello/fallback/a")
            .then()
            .statusCode(200)
            .body(is("globalBlockHandler, ex:null"));

        given()
            .when().get("/hello/fallback2/a")
            .then()
            .statusCode(200)
            .body(is("hello a"));
        given()
            .when().get("/hello/fallback2/b")
            .then()
            .statusCode(200)
            .body(is("greetingFallback: FlowException"));
    }

}
