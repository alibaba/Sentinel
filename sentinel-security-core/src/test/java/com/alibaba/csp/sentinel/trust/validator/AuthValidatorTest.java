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
package com.alibaba.csp.sentinel.trust.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.trust.auth.Rules;
import com.alibaba.csp.sentinel.trust.auth.condition.AuthCondition;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.IpMatcher;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.PortMatcher;
import com.alibaba.csp.sentinel.trust.auth.condition.matcher.StringMatcher;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthRule;
import com.alibaba.csp.sentinel.trust.auth.rule.AuthType;
import com.alibaba.csp.sentinel.trust.auth.rule.JwtRule;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author lwj
 * @since 2.0.0
 */
public class AuthValidatorTest {

    @Test
    public void testValidate() {
        String okToken
            = "eyJhbGciOiJSUzI1NiIsImtpZCI6Im15a2V5IiwidHlwIjoiSldUIn0"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjI2ODk5MDM2NzN9"
            + ".M4o8ziktarFbPIHI9h7zbczbbr8p0xc5kzDVgzhH98Zc3WvEiUf1aNlnJWzeRLKgZe7omk-Jfk7X"
            + "-RCi0QTMQVsCW7xJ6OqfV4ndIHAaf-F7Vbz9AbD08EmlEJCmEMq5l3nih-sTn2wwXg3neddJLHOxy"
            + "-ikGWpf_WnvvDDoExZzHzwGZVGYoanWgDzn5VQxdM-h3ZiB9YNCCAtP3PIi37v69A-23UCLjJxKXMSnmrR93-qwjvD4MCvC4aY-qQ"
            + "-lmU-rlt0otnBKpuYtVviMWAVMGVqI5PdWw4IcTWIQufZwal3albNOlvWnXUT2As0rNIeDC7OclK6tcY5aca7Z7Q";
        String okJwks
            = "{\"keys\": [{\"kty\": \"RSA\", \"alg\": \"RS256\", \"kid\": \"mykey\", \"use\": \"sig\", \"n\": "
            + "\"qXrqGpzWeY5TFtBMklVb0wgMqNV_H-CO78ZIN7NIkUfWocRown4vesgi"
            + "-84RROCOF0lQSuiXi4o8y685cw8FA8ikDL94o9OPeV5ENzfku5tuxCEF5pDURbYDTeg"
            + "-Hawu6NhczKH8vfFKOMUOgvDtD4GezY7SfB4dg2j3Gi3xeOKHsDh2uyJDeNen_coO"
            +
            "-o7pdlLCYC9cbmqxb66hyhFEUT5cfyggLMXo_rKnUfst2oPbWJOgD_UOpsn_qyNM3qdzMsCcANNn0SLIboUNMcDWemxKtZXQS3XvHXdGVQ__OoUOGqyP3hoO9qRcXoxWLZD6Jq7DoJ-UyR8O-nDqn9-UFw==\", \"e\": \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAQ==\"}]}";

        String notToken
            = "eyJhbGciOiJSUzI1NiIsImtpZCI6Im15a2V5IiwidHlwIjoiSldUIn0"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjY4OTkwMzcyMH0"
            +
            ".Wl9MgSjsR13UZKh9i9tt5bhkbWsVJzD4dSiwA8czZND4DJYOOlWqGwCD496aY2H66AyOOKo40b5fM36fVHBTK9eoqeHQI8LNOuJwnq9EoijkFq1dmzdagqBz7pkLyB_J9AKvIlH1TrZXcXtZ5CwdEivi3YjjVo5T-Ot6ME5-XKIdvRo9GzGRSqjko9poL2wyt-t_lWA5aBxtrYRYIOHVDM76ow6fnwX8NwAynySq2uw11mpc7MBuOfRe2U9UyVjjZdDPzEOGpXHCR6gVtO4zzFdeC7P2tp_-i3c09GvORMVnGpb2xhGiuIgG3vbiVEpr--S1739qiwVG5m6btVr3XA";
        String notJwks
            = "{\"keys\": [{\"kty\": \"RSA\", \"alg\": \"RS256\", \"kid\": \"mykey\", \"use\": \"sig\", \"n\": "
            + "\"q27wGbMTXsC8zhC1jDQARMf1L-70vhZuYaGZZacPdTFpRQNDT85P-ygi80jspPJdwprj1IQbdMXGKD"
            +
            "-sZonmqdXfAkg1Muq_YkbQBn_fqmx8ye0nURv7vxhfWb8ONPVFX28o9lCt1dToOvaTVxD39KhDhSwKgilNvrynr2exsHHDVHtsHxDf26rgtX3WB2avH-tQBi1eHfwKKLk1bDX4IMRvDbxOygXTHZrp4xK-nUs5I6VU9FfmGNILhe7-SLQp4jDAM7r0ndAi3noEPrZNd6oa_m0vhiOwWOAocnX4TIquXllqY7CEQc149PSNYa0WqMnax109MizizODdEI2KHw==\", \"e\": \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAQ==\"}]}";

        UnifiedHttpRequest request = new UnifiedHttpRequest.UnifiedHttpRequestBuilder()
            .setPort(8080)
            .setHeaders(new HashMap<String, List<String>>() {{
                put("header1", new ArrayList<String>() {{
                    add("pre12 " + okToken);
                }});
            }})
            .build();

        AuthRule okAuthRule = new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT, new PortMatcher(8080)));
        AuthRule notAuthRule = new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT, new PortMatcher(8081)));

        Map<String, String> fromHeader = new HashMap<String, String>() {{
            put("header1", "pre12 ");
        }};
        JwtRule jwtRule = new JwtRule("", fromHeader, null, null, okJwks, null);

        JwtRule notJwtRule = new JwtRule("", fromHeader, null, null, notJwks, null);

        Rules rules = new Rules(new HashMap<>(), new HashMap<>(), new HashMap<>());

        Assert.assertTrue(AuthValidator.validate(request, rules));

        rules = new Rules(new HashMap<>(), new HashMap<>(), new HashMap<String, JwtRule>() {{
            put("x", jwtRule);
        }});

        Assert.assertTrue(AuthValidator.validate(request, rules));

        rules = new Rules(new HashMap<String, AuthRule>() {{
            put("x", okAuthRule);
        }}, new HashMap<>(), new HashMap<String, JwtRule>() {{
            put("x", jwtRule);
        }});

        Assert.assertTrue(AuthValidator.validate(request, rules));

        rules = new Rules(new HashMap<String, AuthRule>() {{
            put("x", okAuthRule);
        }}, new HashMap<String, AuthRule>() {{
            put("x", notAuthRule);
        }}, new HashMap<String, JwtRule>() {{
            put("x", jwtRule);
        }});

        Assert.assertTrue(AuthValidator.validate(request, rules));

        rules = new Rules(new HashMap<String, AuthRule>() {{
            put("x", notAuthRule);
        }}, new HashMap<String, AuthRule>() {{
            put("x", notAuthRule);
        }}, new HashMap<String, JwtRule>() {{
            put("x", jwtRule);
        }});

        Assert.assertFalse(AuthValidator.validate(request, rules));

        rules = new Rules(new HashMap<String, AuthRule>() {{
            put("x", okAuthRule);
        }}, new HashMap<String, AuthRule>() {{
            put("x", okAuthRule);
        }}, new HashMap<String, JwtRule>() {{
            put("x", jwtRule);
        }});

        Assert.assertFalse(AuthValidator.validate(request, rules));

        request = new UnifiedHttpRequest.UnifiedHttpRequestBuilder()
            .setPort(8080)
            .setHeaders(new HashMap<String, List<String>>() {{
                put("header1", new ArrayList<String>() {{
                    add("pre12 " + notJwks);
                }});
            }})
            .build();
        rules = new Rules(new HashMap<String, AuthRule>() {{
            put("x", okAuthRule);
        }}, new HashMap<String, AuthRule>() {{
            put("x", notAuthRule);
        }}, new HashMap<String, JwtRule>() {{
            put("x", jwtRule);
        }});

        Assert.assertFalse(AuthValidator.validate(request, rules));

    }

    @Test
    public void testValidateJwtRule() {
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setIssuer("iss");
        jwtClaims.setSubject("sub");
        jwtClaims.setAudience("aud1", "aud2");
        jwtClaims.setClaim("azp", "azpvalue");
        jwtClaims.setClaim("abc", "abc1");
        jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(System.currentTimeMillis() + 1000000));

        List<String> audis = new ArrayList<String>() {{
            add("aud1");
            add("aud3");
        }};
        JwtRule jwtRule = new JwtRule(null, null, null, audis, null, null);

        Assert.assertTrue(AuthValidator.validateJwtRule(jwtRule, jwtClaims));

        jwtRule = new JwtRule(null, null, "iss", null, null, null);

        Assert.assertTrue(AuthValidator.validateJwtRule(jwtRule, jwtClaims));

        jwtRule = new JwtRule(null, null, "iss", audis, null, null);
        Assert.assertTrue(AuthValidator.validateJwtRule(jwtRule, jwtClaims));

        jwtRule = new JwtRule(null, null, "iss1", audis, null, null);
        Assert.assertFalse(AuthValidator.validateJwtRule(jwtRule, jwtClaims));

        List<String> audisErr = new ArrayList<String>() {{
            add("aud3");
            add("aud4");
        }};

        jwtRule = new JwtRule(null, null, "iss1", audis, null, null);
        Assert.assertFalse(AuthValidator.validateJwtRule(jwtRule, jwtClaims));

        jwtRule = new JwtRule(null, null, "iss", audisErr, null, null);
        Assert.assertFalse(AuthValidator.validateJwtRule(jwtRule, jwtClaims));

        jwtRule = new JwtRule(null, null, "iss", audis, null, null);
        jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(System.currentTimeMillis() - 1000000));
        Assert.assertFalse(AuthValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    @Test
    public void testValidateRule() {
        UnifiedHttpRequest request = new UnifiedHttpRequest.UnifiedHttpRequestBuilder()
            .setPort(8080)
            .build();

        AuthRule authRuleChild1 = new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT, new PortMatcher(8080)));
        AuthRule authRuleChild2 = new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT, new PortMatcher(8081)));

        AuthRule authRuleAnd = new AuthRule(AuthRule.ChildChainType.AND);
        authRuleAnd.addChildren(authRuleChild1);
        authRuleAnd.addChildren(authRuleChild2);

        Assert.assertFalse(AuthValidator.validateRule(authRuleAnd, request));

        AuthRule authRuleAndNot = new AuthRule(AuthRule.ChildChainType.AND, true);
        authRuleAndNot.addChildren(authRuleChild1);
        authRuleAndNot.addChildren(authRuleChild2);

        Assert.assertTrue(AuthValidator.validateRule(authRuleAndNot, request));

        AuthRule authRuleOr = new AuthRule(AuthRule.ChildChainType.OR);
        authRuleOr.addChildren(authRuleChild1);
        authRuleOr.addChildren(authRuleChild2);

        Assert.assertTrue(AuthValidator.validateRule(authRuleOr, request));

        AuthRule authRuleOrNot = new AuthRule(AuthRule.ChildChainType.OR, true);
        authRuleOrNot.addChildren(authRuleChild1);
        authRuleOrNot.addChildren(authRuleChild2);

        Assert.assertFalse(AuthValidator.validateRule(authRuleOrNot, request));

    }

    @Test
    public void testValidateLeafRule() {
        UnifiedHttpRequest request = new UnifiedHttpRequest.UnifiedHttpRequestBuilder()
            .setDestIp("11.1.1.1")
            .setRemoteIp("12.1.1.1")
            .setSourceIp("13.1.1.1")
            .setHost("www.abc.com")
            .setMethod("put")
            .setPath("/abc/cdf")
            .setPort(8080)
            .setPrincipal("principal")
            .setSni("sni")
            .setHeaders(new HashMap<String, List<String>>() {{
                put("header1", new ArrayList<String>() {{
                    add("pre11");
                    add("pre12");
                }});
                put("header2", new ArrayList<String>() {{
                    add("pre21");
                    add("pre22");
                }});
            }})
            .setParams(new HashMap<String, List<String>>() {{
                put("param1", new ArrayList<String>() {{
                    add("p11");
                    add("12");
                }});
                put("param2", new ArrayList<String>() {{
                    add("p21");
                    add("22");
                }});
            }})
            .build();
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setIssuer("iss");
        jwtClaims.setSubject("sub");
        jwtClaims.setAudience("aud1", "aud2");
        jwtClaims.setClaim("azp", "azpvalue");
        jwtClaims.setClaim("abc", "abc1");
        request.setJwtClaims(jwtClaims);

        AuthRule authRuleTrue = new AuthRule(new AuthCondition(AuthType.DESTINATION_IP, new IpMatcher(32, "11.1.1.1")));
        AuthRule authRuleFalse = new AuthRule(
            new AuthCondition(AuthType.DESTINATION_IP, new IpMatcher(32, "11.1.1.0")));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.REMOTE_IP, new IpMatcher(32, "12.1.1.1")));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.REMOTE_IP, new IpMatcher(32, "12.1.1.0")));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP, new IpMatcher(32, "13.1.1.1")));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.DIRECT_REMOTE_IP, new IpMatcher(32, "13.1.1.0")));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.HOSTS,
            new StringMatcher("www.abc.com", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.HOSTS,
            new StringMatcher("www.abc.com1", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(
            new AuthCondition(AuthType.METHODS, new StringMatcher("put", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(
            new AuthCondition(AuthType.METHODS, new StringMatcher("get", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.URL_PATH,
            new StringMatcher("/abc/cdf", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.URL_PATH,
            new StringMatcher("/abc/cdferr", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT, new PortMatcher(8080)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.DESTINATION_PORT, new PortMatcher(8081)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
            new StringMatcher("principal", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.AUTHENTICATED,
            new StringMatcher("principalerr", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.REQUESTED_SERVER_NAME,
            new StringMatcher("sni", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.REQUESTED_SERVER_NAME,
            new StringMatcher("snierr", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.HEADER, "header1",
            new StringMatcher("pre12", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.HEADER, "header1",
            new StringMatcher("preerr", StringMatcher.MatcherType.EXACT, false)));
        AuthRule authRuleFalse1 = new AuthRule(new AuthCondition(AuthType.HEADER, "header3",
            new StringMatcher("pre1", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse1, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
            new StringMatcher("iss/sub", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.JWT_PRINCIPALS,
            new StringMatcher("iss/err", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.JWT_AUDIENCES,
            new StringMatcher("aud1", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.JWT_AUDIENCES,
            new StringMatcher("aud3", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.JWT_PRESENTERS,
            new StringMatcher("azpvalue", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.JWT_PRESENTERS,
            new StringMatcher("azpvalueerr", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));

        authRuleTrue = new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "abc",
            new StringMatcher("abc1", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse = new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "abc",
            new StringMatcher("abc3", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse1 = new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "abcd",
            new StringMatcher("a", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertTrue(AuthValidator.validateLeafRule(authRuleTrue, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse1, request));

        request = new UnifiedHttpRequest.UnifiedHttpRequestBuilder()
            .setDestIp("11.1.1.1")
            .setRemoteIp("12.1.1.1")
            .setSourceIp("13.1.1.1")
            .setHost("www.abc.com")
            .setMethod("put")
            .setPath("/abc/cdf")
            .setPort(8080)
            .setPrincipal("principal")
            .setSni("sni")
            .setHeaders(null)
            .setParams(null)
            .build();

        authRuleFalse = new AuthRule(
            new AuthCondition(AuthType.HEADER, "abcd", new StringMatcher("a", StringMatcher.MatcherType.EXACT, false)));
        authRuleFalse1 = new AuthRule(new AuthCondition(AuthType.JWT_CLAIMS, "abcd",
            new StringMatcher("a", StringMatcher.MatcherType.EXACT, false)));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse, request));
        Assert.assertFalse(AuthValidator.validateLeafRule(authRuleFalse1, request));

    }

}