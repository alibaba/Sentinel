/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.datasource.xds.client.filiter.lds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.datasource.xds.util.TestUtil;
import com.alibaba.csp.sentinel.trust.auth.condition.AuthCondition;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.IpMatcher;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.PortMatcher;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.StringMatcher;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthRule;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthType;
import com.alibaba.csp.sentinel.trust.auth.rule.JwtRule;

import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author lwj
 * @since 2.0.0
 */
public class AuthLdsFilterTest {

    public static List<HttpFilter> readListenersFromFile(String fileName)
        throws URISyntaxException, IOException, ClassNotFoundException {
        URL serURL = TestUtil.class.getClassLoader().getResource(fileName);
        FileInputStream fileInputStream = new FileInputStream(new File(serURL.toURI()));
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        List<Listener> listeners = (List<Listener>) objectInputStream.readObject();
        List<HttpFilter> httpFilters = AuthLdsFilter.resolveHttpFilter(listeners);
        return httpFilters;
    }

    @Test
    public void testResolveHttpFilter() throws URISyntaxException, IOException, ClassNotFoundException {
        List<HttpFilter> httpFilters = readListenersFromFile("JwtLdsTest.ser");
        List<HttpFilter> http1Filters = readListenersFromFile("RbacLdsTest1.ser");
        List<HttpFilter> http2Filters = readListenersFromFile("RbacLdsTest2.ser");
        assertEquals(30, httpFilters.size());
        assertEquals(25, http1Filters.size());
        assertEquals(30, http2Filters.size());
    }

    /**
     * JwtLdsTest.ser file contains yaml
     * ./resources/yaml/JwtLdsTest.yaml
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     * @throws JoseException
     */
    @Test
    public void testResolveJWT() throws IOException, ClassNotFoundException, URISyntaxException, JoseException {
        List<HttpFilter> httpFilters = readListenersFromFile("JwtLdsTest.ser");
        Map<String, JwtRule> jwtRuleMap = AuthLdsFilter.resolveJWT(httpFilters);
        assertEquals(2, jwtRuleMap.size());
        JwtRule jwtRule0 = null;
        JwtRule jwtRule1 = null;

        for (Map.Entry<String, JwtRule> entry : jwtRuleMap.entrySet()) {
            if (entry.getValue().getIssuer().equals("issuer-foo")) {
                jwtRule0 = entry.getValue();
            }

            if (entry.getValue().getIssuer().equals("issuer-foo1")) {
                jwtRule1 = entry.getValue();
            }
        }

        assertNotNull(jwtRule0);
        assertNotNull(jwtRule1);
        assertEquals(new HashMap<String, String>() {{
            put("header1", "pre1");
            put("header2", "pre2");
        }}, jwtRule0.getFromHeaders());

        assertEquals(new ArrayList<String>() {{
            add("bookstore_android.apps.example.com");
            add("bookstore_web.apps.example.com");
        }}, jwtRule0.getAudiences());

        assertEquals(new ArrayList<String>() {{
            add("parmas1");
            add("parmas2");
        }}, jwtRule0.getFromParams());

        assertNotNull(new JsonWebKeySet(jwtRule0.getJwks()));

        assertEquals(0, jwtRule1.getFromHeaders().size());
        assertEquals(0, jwtRule1.getAudiences().size());
        assertEquals(0, jwtRule1.getFromParams().size());
        assertNotNull(new JsonWebKeySet(jwtRule1.getJwks()));

    }

    /**
     * RbacLdsTest1.ser file contains yaml
     * ./resources/yaml/RbacLdsTest1.yaml
     *
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void testResolveRbac1() throws URISyntaxException, IOException, ClassNotFoundException {
        List<HttpFilter> httpFilters = readListenersFromFile("RbacLdsTest1.ser");
        Map<String, AuthRule> allowAuthRules = new HashMap<>();
        Map<String, AuthRule> denyAuthRules = new HashMap<>();
        AuthLdsFilter.resolveRbac(httpFilters, allowAuthRules, denyAuthRules);
        assertEquals(1, denyAuthRules.size());
        AuthRule allAuthRule = denyAuthRules.get("ns[default]-policy[httpbin]-rule[0]");
        testAllAuthRule(allAuthRule);
    }

    /**
     * RbacLdsTest2.ser file contains yaml
     * ./resources/yaml/RbacLdsTest1.yaml
     * ./resources/yaml/RbacLdsTest2.yaml
     * ./resources/yaml/RbacLdsTest3.yaml
     *
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void testResolveRbac2() throws URISyntaxException, IOException, ClassNotFoundException {
        List<HttpFilter> httpFilters = readListenersFromFile("RbacLdsTest2.ser");
        Map<String, AuthRule> allowAuthRules = new HashMap<>();
        Map<String, AuthRule> denyAuthRules = new HashMap<>();
        AuthLdsFilter.resolveRbac(httpFilters, allowAuthRules, denyAuthRules);
        assertEquals(1, denyAuthRules.size());
        assertEquals(2, allowAuthRules.size());
        AuthRule allAuthRule = denyAuthRules.get("ns[default]-policy[httpbin]-rule[0]");
        testAllAuthRule(allAuthRule);
        allAuthRule = allowAuthRules.get("ns[default]-policy[httpbin1]-rule[0]");
        testAllAuthRule(allAuthRule);
        allAuthRule = allowAuthRules.get("ns[default]-policy[httpbin2]-rule[0]");
        testAllAuthRule(allAuthRule);
    }

    private void testAllAuthRule(AuthRule allAuthRule) {
        String priPrefix = "spiffe://";
        assertEquals(AuthRule.ChildChainType.AND, allAuthRule.getChildChainType());
        List<AuthRule> principalsAndPermissonAuthRuleList = allAuthRule.getChildren();
        assertEquals(2, principalsAndPermissonAuthRuleList.size());
        AuthRule principalAuthRule = principalsAndPermissonAuthRuleList.get(0);
        AuthRule permissionAuthRule = principalsAndPermissonAuthRuleList.get(1);
        assertEquals(AuthRule.ChildChainType.OR, principalAuthRule.getChildChainType());

        //principal
        List<AuthRule> priAuthRuleList = principalAuthRule.getChildren();
        assertEquals(2, priAuthRuleList.size());
        AuthRule priAuthRule1 = priAuthRuleList.get(0);
        AuthRule priAuthRule2 = priAuthRuleList.get(1);
        assertEquals(AuthRule.ChildChainType.AND, priAuthRule1.getChildChainType());
        List<AuthRule> sourceRuleList = priAuthRule1.getChildren();
        assertEquals(19, sourceRuleList.size());
        AuthRule sourceRule = sourceRuleList.get(0);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "principal1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "principal2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(1);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(true, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "notPrincipal1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "notPrincipal2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(2);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("jwtp1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("jwtp2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(3);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(true, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("notjwtp1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("notjwtp2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(4);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/namespace1/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/namespace2/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(5);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(true, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/notNamespace1/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/notNamespace2/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(6);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(32, "12.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(24, "12.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(7);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(true, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(32, "13.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(24, "13.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(8);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(32, "10.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(24, "10.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(9);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(true, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(32, "11.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(24, "11.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(10);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.HEADER, "x1",
                new StringMatcher("whenhead1", StringMatcher.MatcherType.EXACT, true))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.HEADER, "x1",
                new StringMatcher("whenhead2", StringMatcher.MatcherType.EXACT, true))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(11);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(32, "14.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(24, "14.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(12);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(32, "15.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(24, "15.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(13);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/whennamespace1/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/whennamespace2/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(14);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "whenprincipal1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "whenprincipal2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(15);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("whenjwt1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("whenjwt2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(16);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_AUDIENCES,
                new StringMatcher("whenaudience1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_AUDIENCES,
                new StringMatcher("whenaudience2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(17);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRESENTERS,
                new StringMatcher("whenissuer1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRESENTERS,
                new StringMatcher("whenissuer2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(18);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "iss",
                new StringMatcher("whenclaims1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "iss",
                new StringMatcher("whenclaims2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        assertEquals(AuthRule.ChildChainType.AND, priAuthRule2.getChildChainType());
        sourceRuleList = priAuthRule2.getChildren();
        assertEquals(10, sourceRuleList.size());
        sourceRule = sourceRuleList.get(0);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "principal3", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));

        sourceRule = sourceRuleList.get(1);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.HEADER, "x1",
                new StringMatcher("whenhead1", StringMatcher.MatcherType.EXACT, true))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.HEADER, "x1",
                new StringMatcher("whenhead2", StringMatcher.MatcherType.EXACT, true))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(2);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(32, "14.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP,
                new IpMatcher(24, "14.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(3);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(32, "15.1.1.1"))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.REMOTE_IP,
                new IpMatcher(24, "15.1.1.0"))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(4);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/whennamespace1/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(".*/ns/whennamespace2/.*", StringMatcher.MatcherType.REGEX, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(5);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "whenprincipal1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
                new StringMatcher(priPrefix + "whenprincipal2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(6);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("whenjwt1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
                new StringMatcher("whenjwt2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(7);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_AUDIENCES,
                new StringMatcher("whenaudience1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_AUDIENCES,
                new StringMatcher("whenaudience2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(8);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRESENTERS,
                new StringMatcher("whenissuer1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_PRESENTERS,
                new StringMatcher("whenissuer2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        sourceRule = sourceRuleList.get(9);
        assertEquals(AuthRule.ChildChainType.OR, sourceRule.getChildChainType());
        assertEquals(false, sourceRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "iss",
                new StringMatcher("whenclaims1", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "iss",
                new StringMatcher("whenclaims2", StringMatcher.MatcherType.EXACT, false))),
            sourceRule.getChildren().get(1));

        assertEquals(AuthRule.ChildChainType.OR, permissionAuthRule.getChildChainType());

        //principal
        List<AuthRule> perAuthRuleList = permissionAuthRule.getChildren();
        assertEquals(2, priAuthRuleList.size());
        AuthRule perAuthRule1 = perAuthRuleList.get(0);
        AuthRule perAuthRule2 = perAuthRuleList.get(1);
        assertEquals(AuthRule.ChildChainType.AND, perAuthRule1.getChildChainType());
        List<AuthRule> toRuleList = perAuthRule1.getChildren();
        assertEquals(11, toRuleList.size());
        AuthRule toRule = toRuleList.get(0);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.HOSTS,
                new StringMatcher("www.host1.com", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.HOSTS,
                new StringMatcher("www.host2.com", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(1);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(true, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.HOSTS,
                new StringMatcher("www.nothost1.com", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.HOSTS,
                new StringMatcher("www.nothost2.com", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(2);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.METHODS,
                new StringMatcher("get", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.METHODS,
                new StringMatcher("post", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(3);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(true, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.METHODS,
                new StringMatcher("put", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.METHODS,
                new StringMatcher("delete", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(4);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.URL_PATH,
                new StringMatcher("/info1", StringMatcher.MatcherType.PREFIX, false))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.URL_PATH,
                new StringMatcher("/info2", StringMatcher.MatcherType.EXACT, false))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(5);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(true, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.URL_PATH,
                new StringMatcher("/notinfo1", StringMatcher.MatcherType.PREFIX, false))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.URL_PATH,
                new StringMatcher("/notinfo2", StringMatcher.MatcherType.EXACT, false))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(6);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(8080))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(443))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(7);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(true, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(18080))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(1443))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(8);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_IP,
                new IpMatcher(32, "16.1.1.1"))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_IP,
                new IpMatcher(24, "16.1.1.0"))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(9);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(28080))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(2443))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(10);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.REQUESTED_SERVER_NAME,
                new StringMatcher("www.whensni1.com", StringMatcher.MatcherType.EXACT, false))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.REQUESTED_SERVER_NAME,
                new StringMatcher("www.whensni2.com", StringMatcher.MatcherType.EXACT, false))),
            toRule.getChildren().get(1));

        assertEquals(AuthRule.ChildChainType.AND, perAuthRule2.getChildChainType());

        assertEquals(AuthRule.ChildChainType.AND, perAuthRule2.getChildChainType());
        toRuleList = perAuthRule2.getChildren();
        assertEquals(4, toRuleList.size());

        toRule = toRuleList.get(0);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.HOSTS,
                new StringMatcher("www.host3.com", StringMatcher.MatcherType.EXACT, true))),
            toRule.getChildren().get(0));

        toRule = toRuleList.get(1);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_IP,
                new IpMatcher(32, "16.1.1.1"))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_IP,
                new IpMatcher(24, "16.1.1.0"))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(2);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(28080))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT,
                new PortMatcher(2443))),
            toRule.getChildren().get(1));

        toRule = toRuleList.get(3);
        assertEquals(AuthRule.ChildChainType.OR, toRule.getChildChainType());
        assertEquals(false, toRule.isNot());
        assertEquals(new AuthRule(new AuthCondition(AuthType.REQUESTED_SERVER_NAME,
                new StringMatcher("www.whensni1.com", StringMatcher.MatcherType.EXACT, false))),
            toRule.getChildren().get(0));
        assertEquals(new AuthRule(new AuthCondition(AuthType.REQUESTED_SERVER_NAME,
                new StringMatcher("www.whensni2.com", StringMatcher.MatcherType.EXACT, false))),
            toRule.getChildren().get(1));

    }
}