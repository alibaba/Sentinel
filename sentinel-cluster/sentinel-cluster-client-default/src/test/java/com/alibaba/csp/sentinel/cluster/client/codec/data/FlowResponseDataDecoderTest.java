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
package com.alibaba.csp.sentinel.cluster.client.codec.data;

import com.alibaba.csp.sentinel.cluster.response.data.FlowTokenResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class FlowResponseDataDecoderTest {
    @Test
    public void testDecode() {
        ByteBuf buf = Unpooled.buffer();
        FlowResponseDataDecoder decoder = new FlowResponseDataDecoder();
        FlowTokenResponseData data = new FlowTokenResponseData();
        data.setRemainingCount(12);
        data.setWaitInMs(13);
        buf.writeInt(12);
        buf.writeInt(13);
        Assert.assertEquals(decoder.decode(buf), data);
    }
}
