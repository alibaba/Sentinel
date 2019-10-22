package com.alibaba.csp.sentinel.cluster.client.codec.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link PingRequestDataWriter}
 *
 * @author nick-tan
 * @version 1.0
 * @date 2019/10/19
 */
public class PingRequestDataWriterTest {

	/**
	 * normal
	 */
	@Test
	public void writeTo() {
		ByteBuf buf = Unpooled.buffer();
		PingRequestDataWriter writer = new PingRequestDataWriter();
		writer.writeTo("test", buf);

		buf.release();

	}

	/**
	 * null entity
	 */
	@Test
	public void writeToNullEntity() {
		ByteBuf buf = Unpooled.buffer();
		PingRequestDataWriter writer = new PingRequestDataWriter();
		writer.writeTo("", buf);

		buf.release();
	}

	/**
	 * null ByteBuf
	 */
	@Test
	public void writeToNullByteBuf() {
		PingRequestDataWriter writer = new PingRequestDataWriter();
		writer.writeTo("test", null);
	}
}