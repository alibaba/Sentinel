package com.alibaba.csp.sentinel.cluster.client.codec.data;

import com.alibaba.csp.sentinel.cluster.request.data.FlowRequestData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link FlowRequestDataWriter}
 *
 * @author nick-tan
 * @version 1.0
 * @date 2019/10/19
 */
public class FlowRequestDataWriterTest {

    /**
     * normal
     */
    @Test
    public void writeTo() {
        FlowRequestDataWriter writer = new FlowRequestDataWriter();

        FlowRequestData entity = new FlowRequestData();
        entity.setCount(1);
        entity.setFlowId(2L);
        entity.setPriority(true);

        ByteBuf buf = Unpooled.buffer();
        writer.writeTo(entity, buf);

        Assert.assertEquals(13, buf.readableBytes());

        //
        Assert.assertEquals(2L, buf.readLong());
        Assert.assertEquals(1, buf.readInt());
        Assert.assertEquals(true, buf.readBoolean());

        buf.release();
    }

    /**
     * illegal ByteBuf
     */
    @Test(expected = NullPointerException.class)
    public void writeToIllegalByteBuf() {
        FlowRequestDataWriter writer = new FlowRequestDataWriter();

        FlowRequestData entity = new FlowRequestData();
        entity.setCount(1);
        entity.setFlowId(1L);
        entity.setPriority(true);
        writer.writeTo(entity, null);
    }

    /**
     * illegal FlowRequestData
     */
    @Test(expected = NullPointerException.class)
    public void writeToIllegalFlowRequestData() {
        FlowRequestDataWriter writer = new FlowRequestDataWriter();

        ByteBuf buf = Unpooled.buffer();
        writer.writeTo(null, buf);
        buf.release();
    }
}