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
package com.alibaba.csp.sentinel.trust.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.trust.auth.rule.JwtRule;

import org.jose4j.jwt.JwtClaims;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author lwj
 * @since 2.0.0
 */
public class JwtUtilTest {

    @Test
    public void testGetTokenFromJwtRule() {
        Map<String, String> fromHeaders = new HashMap<String, String>() {{
            put("header1", "pre1");
            put("header2", "pre2");
        }};
        List<String> fromParams = new ArrayList<String>() {{
            add("param1");
            add("param2");
        }};
        Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
            put("header1", null);
            put("header0", new ArrayList<String>() {{
                add("pre");
            }});
            put("header1", new ArrayList<String>() {{
                add("pretokenheader1");
            }});
            put("header2", new ArrayList<String>() {{
                add("pre2token2");
            }});
        }};

        Map<String, List<String>> noHeaders = new HashMap<String, List<String>>() {{
            put("header1", null);
            put("header0", new ArrayList<String>() {{
                add("pre");
            }});
            put("header1", new ArrayList<String>() {{
                add("pretokenheader1");
            }});
            put("header2", new ArrayList<String>() {{
                add("pre3token2");
            }});
        }};

        Map<String, List<String>> params = new HashMap<String, List<String>>() {{
            put("param1", null);
            put("parma", new ArrayList<String>() {{
                add("pre");
            }});
            put("param1", new ArrayList<String>() {{
                add("tokenparam");
            }});
        }};

        Map<String, List<String>> noParams = new HashMap<String, List<String>>() {{
            put("param1", null);
            put("parma", new ArrayList<String>() {{
                add("pre");
            }});
            put("param3", new ArrayList<String>() {{
                add("tokenparam");
            }});
        }};

        assertEquals("", JwtUtil.getTokenFromJwtRule(params, headers, null));

        JwtRule nullJwtRule = new JwtRule("", null, "", null, "", null);

        assertEquals("", JwtUtil.getTokenFromJwtRule(params, headers, nullJwtRule));

        JwtRule nullHeaderJwtRule = new JwtRule("", null, "", null, "", fromParams);

        assertEquals("tokenparam", JwtUtil.getTokenFromJwtRule(params, headers, nullHeaderJwtRule));

        JwtRule nullParamJwtRule = new JwtRule("", fromHeaders, "", null, "", null);

        assertEquals("token2", JwtUtil.getTokenFromJwtRule(params, headers, nullParamJwtRule));

        JwtRule allJwtRule = new JwtRule("", fromHeaders, "", null, "", fromParams);

        assertEquals("", JwtUtil.getTokenFromJwtRule(null, null, allJwtRule));

        assertEquals("token2", JwtUtil.getTokenFromJwtRule(null, headers, allJwtRule));
        assertEquals("", JwtUtil.getTokenFromJwtRule(null, noHeaders, allJwtRule));
        assertEquals("", JwtUtil.getTokenFromJwtRule(noParams, noHeaders, allJwtRule));

        assertEquals("", JwtUtil.getTokenFromJwtRule(noParams, null, allJwtRule));
        assertEquals("tokenparam", JwtUtil.getTokenFromJwtRule(params, noHeaders, allJwtRule));

    }

    @Test
    public void testExtractJwtClaims() {
        String token
            = "eyJhbGciOiJSUzI1NiIsImtpZCI6Im15a2V5IiwidHlwIjoiSldUIn0"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
            +
            ".neCPSh0cOM4XFze8BFM3lbAqh3WRdKNTPZ1ZpWL1W3BB4OckPKRGn9S72O_wTWpCesnikN0OG7sk2fpQq_s6Ls6YiQHii61e5Km54H1xlqY2FRZXaho9TVSJVv_2xNYczCBVZizXkXt_VTPwcE_qeUDb90NMgZ9UBv_Hj83g6unN2OgM9HwYfxVwlw4G7pqq8tQo5686aE2KnoTgw_TtE6oULSORyYVBK-MWUfmSv_zqfTAH2A0R2_ne1FYKlVEo-mao_ix8ocK0eqtxBbGx7dMysR3ON1MeRDTW2AVRW4LIxDG8_obVRzhnTcx0W7zFJPYastPnPo8t78nx7nGEIA";
        String jwks
            = "{\"keys\": [{\"kty\": \"RSA\", \"alg\": \"RS256\", \"kid\": \"mykey\", \"use\": \"sig\", \"n\": "
            + "\"njX2CK2OdZwFVcDfQYrcUlu5Sede1y6rGIcc270aO_Ga1BChAyy1Gj-mzrIJGDZJPmsc_I8Svgy6LuXLXLJHGby"
            +
            "-QQVkiWGvTpazWKm3JQG7RXYvfvnxOn9GAUbcX65p7dSkY3KpDaJiQaV08lCWhx9qX304wIIWyL0maXGB8PkDtJHncPnlhEZoU7Kcm_Tra0QLk1f-rHQc5U7XfQxMvoexQ6QCdSQT39kdKn9P5ubu_rK8c62qan4FFx-qcMrlvcB9Jom7JMIpSYDSewev37FYyUr_EZ7nXKFYXJB6jtXc-pGsCY3MOy9Pqi43ZjqKatjSO_UlX8ReOBMNL7QBlQ==\", \"e\": \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAQ==\"}]}";
        assertNull(JwtUtil.extractJwtClaims(null, token));
        assertNull(JwtUtil.extractJwtClaims(jwks, null));
        JwtClaims jwtClaims = JwtUtil.extractJwtClaims(jwks, token);
        assertEquals("1234567890", jwtClaims.getClaimValueAsString("sub"));
        assertEquals("John Doe", jwtClaims.getClaimValueAsString("name"));
        assertEquals("1516239022", jwtClaims.getClaimValueAsString("iat"));
    }

}