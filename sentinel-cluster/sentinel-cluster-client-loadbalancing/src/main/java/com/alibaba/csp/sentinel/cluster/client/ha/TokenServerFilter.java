package com.alibaba.csp.sentinel.cluster.client.ha;

import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;

import java.util.List;

/**
 * token server filter,
 *
 * @author icodening
 * @date 2022.03.17
 */
@FunctionalInterface
public interface TokenServerFilter {

    /**
     * filter the given token servers
     * @param tokenServers token servers
     * @return filtered token server
     */
    List<TokenServerDescriptor> filter(List<TokenServerDescriptor> tokenServers);
}
