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
package com.alibaba.csp.sentinel.demo.datasource.etcd;


import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;

/**
 * Etcd config sender for demo.
 *
 * @author lianglin
 * @since 1.7.0
 */
public class EtcdConfigSender {

    public static void main(String[] args) throws InterruptedException {


        String rule_key = "sentinel_demo_rule_key";

        Client client = Client.builder()
                .endpoints("http://127.0.0.1:2379")
                .user(ByteSequence.from("root".getBytes()))
                .password(ByteSequence.from("12345".getBytes()))
                .build();
        final String rule = "[\n"
                + "  {\n"
                + "    \"resource\": \"TestResource\",\n"
                + "    \"controlBehavior\": 0,\n"
                + "    \"count\": 5.0,\n"
                + "    \"grade\": 1,\n"
                + "    \"limitApp\": \"default\",\n"
                + "    \"strategy\": 0\n"
                + "  }\n"
                + "]";
        client.getKVClient()
                .put(ByteSequence.from(rule_key.getBytes()), ByteSequence.from(rule.getBytes()));

        System.out.println("setting rule success");
        Thread.sleep(10000);

    }

}
