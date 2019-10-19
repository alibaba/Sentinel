package com.alibaba.csp.sentinel.cluster.client.codec.data;

import com.alibaba.csp.sentinel.cluster.response.data.FlowTokenResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link FlowResponseDataDecoder}
 * @author nick-tan
 * @version 1.0
 * @date 2019/10/19
 */
public class FlowResponseDataDecoderTest {

    @Test
    public void decode() {
        FlowResponseDataDecoder decoder=new FlowResponseDataDecoder();
        ByteBuf buf=Unpooled.buffer();
        FlowTokenResponseData response=decoder.decode(buf);

        Assert.assertNotNull(response);

        buf.release();

    }

    @Test
    public void decodeWithReadableBytesOf8() {
        FlowResponseDataDecoder decoder=new FlowResponseDataDecoder();
        ByteBuf byteBuf=Unpooled.buffer();
        byteBuf.writeInt(1);
        byteBuf.writeInt(2);
        FlowTokenResponseData response=decoder.decode(byteBuf);

        Assert.assertNotNull(response);

        byteBuf.release();
    }

    @Test
    public void decodeWithReadableBytesLargerThan8() {
        FlowResponseDataDecoder decoder=new FlowResponseDataDecoder();
        ByteBuf byteBuf=Unpooled.buffer();
        byteBuf.writeInt(1);
        byteBuf.writeInt(2);
        byteBuf.writeInt(3);
        FlowTokenResponseData response=decoder.decode(byteBuf);
        Assert.assertNotNull(response);

        byteBuf.release();
    }

    @Test
    public void decodeWithReadableByteLesserThan8() {
        FlowResponseDataDecoder decoder=new FlowResponseDataDecoder();
        ByteBuf byteBuf=Unpooled.buffer();
        byteBuf.writeInt(3);
        FlowTokenResponseData response=decoder.decode(byteBuf);
        Assert.assertNotNull(response);

        byteBuf.release();
    }


}