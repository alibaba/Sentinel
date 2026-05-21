package com.alibaba.csp.sentinel.transport.endpoint;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EndpointTest {
    @Test
    public void testToStringIPv4() {
        Endpoint e = new Endpoint(Protocol.HTTP, "127.0.0.1", 8080);
        assertEquals("Endpoint{protocol=HTTP, host='127.0.0.1', port=8080}", e.toString());
    }

    @Test
    public void testToStringIPv6() {
        Endpoint e = new Endpoint(Protocol.HTTP, "fe80::1", 8080);
        // Endpoint#toString doesn't modify the host, so IPv6 remains unbracketed
        assertEquals("Endpoint{protocol=HTTP, host='fe80::1', port=8080}", e.toString());
    }

    @Test
    public void testToStringAlreadyBracketed() {
        Endpoint e = new Endpoint(Protocol.HTTPS, "[fe80::2]", 443);
        assertEquals("Endpoint{protocol=HTTPS, host='[fe80::2]', port=443}", e.toString());
    }

    @Test
    public void testToStringNullHost() {
        Endpoint e = new Endpoint(Protocol.HTTP, null, 0);
        assertEquals("Endpoint{protocol=HTTP, host='null', port=0}", e.toString());
    }
}
