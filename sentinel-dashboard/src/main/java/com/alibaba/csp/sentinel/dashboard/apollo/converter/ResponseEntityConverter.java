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
package com.alibaba.csp.sentinel.dashboard.apollo.converter;

import com.alibaba.csp.sentinel.dashboard.apollo.entity.ConsumerRole;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

/**
 * @author wxq
 */
public class ResponseEntityConverter {

    private static String toString(ConsumerRole[] consumerRoles) {
        if (null == consumerRoles) {
            return null;
        } else {
            return Arrays.toString(consumerRoles);
        }
    }

    public static ResponseEntity<String> convert2String(ResponseEntity<ConsumerRole[]> responseEntity) {
        HttpStatus httpStatus = responseEntity.getStatusCode();
        HttpHeaders httpHeaders = responseEntity.getHeaders();

        String body = toString(responseEntity.getBody());
        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>(body, httpHeaders, httpStatus);

        return stringResponseEntity;
    }

}
